package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

public class TransferChecker {

    static Logger logger = LoggerFactory.getLogger(TransferChecker.class);

    public static ArrayList<ItemHandler> run() {
        ArrayList<ItemHandler> transferredItems = new ArrayList<>();

        for (String transferFinishedFileName : findTransferFinishedFiles()) {
            ItemHandler transferredItem = processTransferFinishedFile(transferFinishedFileName);
            if (transferredItem != null) {
                transferredItems.add(transferredItem);
            }
        }

        return transferredItems;
    }

    public static String[] findTransferFinishedFiles() {
        File transferFinishedDir = new File(Config.TRANSFER_FINISHED_DIR);

        return transferFinishedDir.list();
    }

    public static ItemHandler processTransferFinishedFile(String transferFinishedFileName) {
        try {
            JSONObject finishedFileAsJson = JobFile.readFinishedFile(transferFinishedFileName, "transfer");
            JobFile.deleteFinishedFile(transferFinishedFileName, "transfer");

            String recordId = finishedFileAsJson.getString("id");
            String assetType = finishedFileAsJson.getString("assetType");
            boolean success = finishedFileAsJson.getBoolean("success");
            String error = finishedFileAsJson.getString("error");

            ItemHandler transferredItem = CumConnection.searchForItemById(recordId);

            if (transferredItem == null) {
                logger.error("Item for finish file not found: " + transferFinishedFileName);
                return null;
            }

            if (assetType.equals("browse")) {
                if (success) {
                    transferredItem.log("Browse asset transfer confirmed.");
                    transferredItem.setLrArchivingStatus("Transferred");
                } else {
                    transferredItem.log("Browse asset transfer failed: " + error);
                    transferredItem.setLrArchivingStatus("Transfer_failed");
                }
            } else {
                if (success) {
                    transferredItem.log("HR asset transfer confirmed.");
                    transferredItem.setHrArchivingStatus("Transferred");
                } else {
                    transferredItem.log("HR asset transfer failed: " + error);
                    transferredItem.setHrArchivingStatus("Transfer_failed");
                }
            }

            transferredItem.save();

            if (transferredItem.getAborted()) {
                transferredItem.abort("Approval");
                transferredItem.save();
                return transferredItem;
            }

            if (transferredItem.getLrArchivingStatus().equals("Transferred")) {
                if (transferredItem.getHrArchivingStatus().equals("Transferred") ||
                        !transferredItem.hasHrAsset()) {

                    transferredItem.updateAssetRef(Config.TRANSFER_NEW_ASSET_DIR, transferredItem.getAssetName(), transferredItem.getHighResName());
                    transferredItem.save();

                    transferredItem.updateMetadataFromAsset();

                    transferredItem.setIngestStatus("Transferred");
                    transferredItem.log("Transfer finished.");
                    transferredItem.log("Awaiting approval.");

                }
            }

            transferredItem.save();

            return transferredItem;

        } catch (Exception e) {
            logger.error("Error processing json file", e);
            return null;
        }
    }
}