package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

public class StorageChecker {

    static Logger logger = LoggerFactory.getLogger(StorageChecker.class);

    public static ArrayList<ItemHandler> run() {
        ArrayList<ItemHandler> storedItems = new ArrayList<>();

        for (String storageFinishedFileName : findStorageFinishedFiles()) {
            ItemHandler storedItem = processStorageFinishedFile(storageFinishedFileName);
            if (storedItem != null) {
                storedItems.add(storedItem);
            }
        }

        return storedItems;
    }

    public static String[] findStorageFinishedFiles() {
        File storageFinishedDir = new File(Config.STORAGE_FINISHED_DIR);

        return storageFinishedDir.list();
    }

    public static ItemHandler processStorageFinishedFile(String storageFinishedFileName) {
        try {
            JSONObject finishedFileAsJson = JobFile.readFinishedFile(storageFinishedFileName, "storage");
            JobFile.deleteFinishedFile(storageFinishedFileName, "storage");

            String recordId = finishedFileAsJson.getString("id");
            String assetType = finishedFileAsJson.getString("assetType");
            boolean success = finishedFileAsJson.getBoolean("success");
            String error = finishedFileAsJson.getString("error");

            ItemHandler storedItem = CumConnection.searchForItemById(recordId);

            if (storedItem == null) {
                logger.error("Item for finish file not found: " + storageFinishedFileName);
                return null;
            }

            if (assetType.equals("browse")) {
                if (success) {
                    storedItem.log("Browse asset storage confirmed.");
                    storedItem.setLrArchivingStatus("Stored");
                } else {
                    storedItem.log("Browse asset storage failed: " + error);
                    storedItem.setLrArchivingStatus("Storage_failed");
                }
            } else {
                if (success) {
                    storedItem.log("HR asset storage confirmed.");
                    storedItem.setHrArchivingStatus("Stored");
                } else {
                    storedItem.log("HR asset encryption failed: " + error);
                    storedItem.setHrArchivingStatus("Storage_failed");
                }
            }

            storedItem.save();

            if (storedItem.getLrArchivingStatus().equals("Stored")) {
                if (storedItem.getHrArchivingStatus().equals("Stored") ||
                        !storedItem.hasHrAsset()) {
                    storedItem.setArchivingStatus("Stored");
                    storedItem.log("Storage finished.");
                }
            }

            storedItem.save();

            return storedItem;

        } catch (Exception e) {
            logger.error("Error processing json file", e);
            return null;
        }
    }
}
