package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ApproveChecker {

    static Logger logger = LoggerFactory.getLogger(ApproveChecker.class);

    public static ArrayList<ItemHandler> run() {
        ArrayList<ItemHandler> approvedItems = CumConnection.getItemsFromArchivingStatus("Approved");

        for (ItemHandler approvedItem : approvedItems) {
            if (!approvedItem.getIngestStatus().equals("N/A")) {
                approvedItem.setIngestStatus("Finished");
            }

            if (approvedItem.getAborted()) {
                approvedItem.abort("Encryption");
                approvedItem.save();
                continue;
            }

            approvedItem.setArchivingStatus("Confirmed");

            makeEncryptTodo(approvedItem, "browse");

            if (approvedItem.hasHrAsset()) {
                makeEncryptTodo(approvedItem, "hr");
            }

            approvedItem.log("Approval confirmed.");
            approvedItem.log("In queue for encryption.");
            approvedItem.save();
        }

        ArrayList<ItemHandler> lrResetItems = CumConnection.getItemsFromLrArchivingStatus("Encryption_reset");
        ArrayList<ItemHandler> hrResetItems = CumConnection.getItemsFromHrArchivingStatus("Encryption_reset");

        for (ItemHandler lrResetItem : lrResetItems) {

            lrResetItem.setLrArchivingStatus("Ready");

            makeEncryptTodo(lrResetItem, "browse");
            lrResetItem.log("In queue for encryption.");
            lrResetItem.save();

            approvedItems.add(lrResetItem);
        }

        for (ItemHandler hrResetItem : hrResetItems) {

            hrResetItem.setHrArchivingStatus("Ready");

            makeEncryptTodo(hrResetItem, "hr");
            hrResetItem.log("In queue for encryption.");
            hrResetItem.save();

            approvedItems.add(hrResetItem);
        }

        return  approvedItems;
    }

    public static void makeEncryptTodo(ItemHandler item, String assetType) {
        JSONObject todoAsJson = new JSONObject();

        try {
            todoAsJson.put("id", item.getId());
            todoAsJson.put("assetDir", item.parseAssetDirFromAssetRef());

            if (assetType.equals("browse")) {
                todoAsJson.put("assetFilename", item.getAssetName());
                todoAsJson.put("saveSource", item.getSaveSource());
            } else {
                todoAsJson.put("assetFilename", item.getHighResName());
                todoAsJson.put("saveSource", false);
            }

            todoAsJson.put("catalogKey", item.catalog.key);
            todoAsJson.put("assetType", assetType);
            todoAsJson.put("isBackupFile", false);
        } catch (Exception e) {
            logger.error("Error creating json file", e);
            return;
        }

        JobFile.createTodoFile(todoAsJson, item.getId(), "encrypt", assetType);
    }
}
