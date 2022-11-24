package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

public class EncryptChecker {

    static Logger logger = LoggerFactory.getLogger(EncryptChecker.class);

    public static ArrayList<ItemHandler> run() {
        ArrayList<ItemHandler> encryptedItems = new ArrayList<>();

        for (String encryptFinishedFileName : findEncryptFinishedFiles()) {
            ItemHandler encryptedItem = processEncryptFinishedFile(encryptFinishedFileName);
            if (encryptedItem != null) {
                encryptedItems.add(encryptedItem);
            }
        }

        ArrayList<ItemHandler> lrResetItems = CumConnection.getItemsFromLrArchivingStatus("Storage_reset");
        ArrayList<ItemHandler> hrResetItems = CumConnection.getItemsFromHrArchivingStatus("Storage_reset");

        for (ItemHandler lrResetItem : lrResetItems) {

            lrResetItem.setLrArchivingStatus("Encrypted");

            makeStorageTodo(lrResetItem, "browse");
            lrResetItem.log("In queue for storage.");
            lrResetItem.save();

            encryptedItems.add(lrResetItem);
        }

        for (ItemHandler hrResetItem : hrResetItems) {

            hrResetItem.setHrArchivingStatus("Encrypted");

            makeStorageTodo(hrResetItem, "hr");
            hrResetItem.log("In queue for storage.");
            hrResetItem.save();

            encryptedItems.add(hrResetItem);
        }

        return encryptedItems;
    }

    public static String[] findEncryptFinishedFiles() {
        File encryptFinishedDir = new File(Config.ENCRYPT_FINISHED_DIR);

        return encryptFinishedDir.list();
    }

    public static ItemHandler processEncryptFinishedFile(String encryptFinishedFileName) {
        try {
            JSONObject finishedFileAsJson = JobFile.readFinishedFile(encryptFinishedFileName, "encrypt");
            JobFile.deleteFinishedFile(encryptFinishedFileName, "encrypt");

            if (finishedFileAsJson.getBoolean("isBackupFile")) {
                String filename = finishedFileAsJson.getString("assetFilename");
                makeBackupStorageTodo(
                        filename,
                        finishedFileAsJson.getString("assetDir")
                );
                logger.info(filename + " in queue for storage.");
                return null;
            }

            String recordId = finishedFileAsJson.getString("id");
            String assetType = finishedFileAsJson.getString("assetType");
            boolean success = finishedFileAsJson.getBoolean("success");
            String error = finishedFileAsJson.getString("error");

            ItemHandler encryptedItem = CumConnection.searchForItemById(recordId);

            if (encryptedItem == null) {
                logger.error("Item for finish file not found: " + encryptFinishedFileName);
                return null;
            }

            if (assetType.equals("browse")) {
                if (success) {
                    encryptedItem.log("Browse asset encryption confirmed.");
                    encryptedItem.setLrArchivingStatus("Encrypted");
                } else {
                    encryptedItem.log("Browse asset encryption failed: " + error);
                    encryptedItem.setLrArchivingStatus("Encryption_failed");
                }
            } else {
                if (success) {
                    encryptedItem.log("HR asset encryption confirmed.");
                    encryptedItem.setHrArchivingStatus("Encrypted");
                } else {
                    encryptedItem.log("HR asset encryption failed: " + error);
                    encryptedItem.setHrArchivingStatus("Encryption_failed");
                }
            }

            encryptedItem.save();

            if (encryptedItem.getAborted()) {
                encryptedItem.abort("Storage");
                encryptedItem.save();
                return encryptedItem;
            }

            if (encryptedItem.getLrArchivingStatus().equals("Encrypted")) {
                if (encryptedItem.getHrArchivingStatus().equals("Encrypted") ||
                        !encryptedItem.hasHrAsset()) {
                    encryptedItem.setArchivingStatus("Encrypted");
                    encryptedItem.log("Encryption finished.");

                    makeStorageTodo(encryptedItem, "browse");

                    if (encryptedItem.hasHrAsset()) {
                        makeStorageTodo(encryptedItem, "hr");
                    }

                    encryptedItem.log("In queue for storage.");
                }
            }

            encryptedItem.save();

            return encryptedItem;

        } catch (Exception e) {
            logger.error("Error processing json file", e);
            return null;
        }
    }

    public static void makeBackupStorageTodo(String filename, String fileDir) {
        JSONObject todoAsJson = new JSONObject();

        try {
            todoAsJson.put("id", "BACKUPFILE");
            todoAsJson.put("assetDir", fileDir);
            todoAsJson.put("assetFilename", filename + ".gpg");
            todoAsJson.put("saveSource", false);
            todoAsJson.put("catalogKey", "NA");
            todoAsJson.put("assetType", "NA");
            todoAsJson.put("isBackupFile", true);
        } catch (Exception e) {
            logger.error("Error creating json file", e);
            return;
        }

        JobFile.createTodoFile(todoAsJson, "", "storage", "BACKUP");
    }

    public static void makeStorageTodo(ItemHandler item, String assetType) {
        JSONObject todoAsJson = new JSONObject();

        try {
            todoAsJson.put("id", item.getId());

            if (assetType.equals("browse")) {
                todoAsJson.put("assetFilename", item.getAssetName() + ".gpg");
            } else {
                todoAsJson.put("assetFilename", item.getHighResName()  + ".gpg");
            }

            todoAsJson.put("assetType", assetType);
            todoAsJson.put("isBackupFile", false);

        } catch (Exception e) {
            logger.error("Error creating json file", e);
            return;
        }

        JobFile.createTodoFile(todoAsJson, item.getId(), "storage", assetType);
    }
}
