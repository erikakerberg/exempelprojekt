package se.sunstone.msa;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Runner {
    public static void main (String[] args) throws InterruptedException {
        String rootPath;

        if (args.length != 0) {
            Config.loadProperties(args[0]);
        } else {
            rootPath = System.getProperty("user.dir").replace("\\bin", "");

            System.setProperty("manager.log.location.1", rootPath + "\\log\\manager.log");
            System.setProperty("manager.log.location.2", rootPath + "\\log\\manager_info.log");

            Config.loadProperties(rootPath + "\\conf\\config.properties");
            PropertyConfigurator.configure(rootPath + "\\conf\\manager-logging.properties");
        }

        Logger logger = LoggerFactory.getLogger(Runner.class);

        try {
            CumConnection.startConnection();
        } catch (Exception e) {
            logger.error("Error connecting to server", e);
            return;
        }

        int runCount = 0;

        while (true) {
            logger.debug("Run: " + runCount);

            BackupChecker.run();

            ArrayList<ItemHandler> deletedItems = DeleteChecker.run();

            for (ItemHandler deletedItem : deletedItems) {
                logger.info("Deleted assets for " + deletedItem.getDisplayString());
            }

            ArrayList<ItemHandler> storedItems = StorageChecker.run();

            for (ItemHandler storedItem : storedItems) {
                logger.info("Completed storage of " + storedItem.getDisplayString());
            }

            ArrayList<ItemHandler> encryptedItems = EncryptChecker.run();

            for (ItemHandler encryptedItem : encryptedItems) {
                logger.info("Completed encryption of " + encryptedItem.getDisplayString());
            }

            ArrayList<ItemHandler> approvedItems = ApproveChecker.run();

            for (ItemHandler approvedItem : approvedItems) {
                logger.info("Confirmed approval of " + approvedItem.getDisplayString());
            }

            ArrayList<ItemHandler> transferredItems = TransferChecker.run();

            for (ItemHandler transferredItem : transferredItems) {
                logger.info("Completed transfer of " + transferredItem.getDisplayString());
            }

            ArrayList<ItemHandler> ingestedItems = IngestChecker.run();

            for (ItemHandler ingestedItem : ingestedItems) {
                logger.info("Completed ingest of " + ingestedItem.getDisplayString());
            }

            runCount += 1;

            Thread.sleep(5000);
        }
    }
}
