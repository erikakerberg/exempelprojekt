package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

public class IngestChecker {

    static Logger logger = LoggerFactory.getLogger(IngestChecker.class);

    public static ArrayList<ItemHandler> run() {

        ArrayList<ItemHandler> ingestedItems = findIngestedItems();

        for (ItemHandler ingestedItem : ingestedItems) {

            if (ingestedItem.getAborted()) {
                ingestedItem.abort("Transfer");
                ingestedItem.save();
                continue;
            }

            String ingestedAssetRef =
                    "\\\\" + Config.TRANSFER_SMB_HOST +
                    "\\" + Config.METUS_SHARE_NAME +
                    "\\" + Config.INGESTED_ASSET_DIR_NAME;

            ingestedItem.updateAssetRef(ingestedAssetRef, ingestedItem.getAssetName(), ingestedItem.getHighResName());
            ingestedItem.save();

            ingestedItem.updateMetadataFromAsset();

            ingestedItem.setIngestStatus("Ingested");

            makeTransferTodo(ingestedItem, "browse");

            if (ingestedItem.hasHrAsset()) {
                makeTransferTodo(ingestedItem, "hr");
            }

            ingestedItem.log("Ingestion finished.");
            ingestedItem.log("In queue for transfer.");
            ingestedItem.save();

        }

        ArrayList<ItemHandler> lrResetItems = CumConnection.getItemsFromHrArchivingStatus("Transfer_reset");
        ArrayList<ItemHandler> hrResetItems = CumConnection.getItemsFromHrArchivingStatus("Transfer_reset");

        for (ItemHandler lrResetItem : lrResetItems) {

            lrResetItem.setIngestStatus("Ingested");

            makeTransferTodo(lrResetItem, "browse");

            lrResetItem.log("In queue for transfer.");

            ingestedItems.add(lrResetItem);
        }

        for (ItemHandler hrResetItem : hrResetItems) {

            hrResetItem.setIngestStatus("Ingested");

            makeTransferTodo(hrResetItem, "hr");

            hrResetItem.log("In queue for transfer.");

            ingestedItems.add(hrResetItem);
        }

        return ingestedItems;
    }

    public static ArrayList<ItemHandler> findIngestedItems() {
        ArrayList<ItemHandler> ingestedItems = new ArrayList<>();

        ArrayList<ItemHandler> ingestingItems = CumConnection.getItemsFromIngestStatus("Ingesting");

        if (ingestingItems.size() == 0) {
            return new ArrayList<>();
        }

        File ingestFinishedFile = new File(Config.INGEST_MIRRORED_DIR);

        if (!ingestFinishedFile.exists()) {
            logger.error("Ingest finished directory not found.");
            return new ArrayList<>();
        }

        String[] ingestedAssetFilenames = ingestFinishedFile.list();

        if (ingestedAssetFilenames == null) {
            return new ArrayList<>();
        }

        for (ItemHandler ingestingItem : ingestingItems) {
            File finishedFile = new File(
                    Config.INGEST_MIRRORED_DIR,
                    ingestingItem.getAssetName()
            );

            if (!finishedFile.exists()) {
                continue;
            }

            finishedFile.delete();

            if (!ingestingItem.getHighResName().equals("N/A")) {
                File highResFinishedFile = new File(
                        Config.INGEST_MIRRORED_DIR,
                        ingestingItem.getHighResName()
                );

                if (!highResFinishedFile.exists()) {
                    continue;
                }

                highResFinishedFile.delete();
            }

            ingestedItems.add(ingestingItem);
        }

        return ingestedItems;
    }

    public static void makeTransferTodo(ItemHandler item, String assetType) {
        JSONObject todoAsJson = new JSONObject();

        try {
            todoAsJson.put("id", item.getId());
            if (assetType.equals("browse")) {
                todoAsJson.put("assetFilename", item.getAssetName());
            } else {
                todoAsJson.put("assetFilename", item.getHighResName());
            }
            todoAsJson.put("assetType", assetType);
        } catch (Exception e) {
            logger.error("Error creating json file", e);
            return;
        }

        JobFile.createTodoFile(todoAsJson, item.getId(), "transfer", assetType);
    }
}
