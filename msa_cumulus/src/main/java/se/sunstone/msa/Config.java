package se.sunstone.msa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Config {

    static String CUMULUS_HOST_IP;
    static String CUMULUS_USERNAME;
    static String CUMULUS_PASSWORD;

    static int CUMULUS_NUMBER_OF_CATALOGS;
    static ArrayList<String> CUMULUS_CATALOG_NAMES = new ArrayList<>();
    static ArrayList<String> CUMULUS_CATALOG_KEYS = new ArrayList<>();

    static String CUMULUS_BACKUPS_DIR;
    static String CUMULUS_BACKUP_TRACK_FILE_DIR;

    static String RECORD_NAME_FIELD_NAME;
    static String ID_FIELD_NAME;
    static String INGEST_STATUS_FIELD_NAME;
    static String ARCHIVING_STATUS_FIELD_NAME;
    static String LR_ARCHIVING_STATUS_FIELD_NAME;
    static String HR_ARCHIVING_STATUS_FIELD_NAME;
    static String LOG_FIELD_NAME;
    static String ASSET_REF_FIELD_NAME;
    static String ASSET_NAME_FIELD_NAME;
    static String HIGH_RES_NAME_FIELD_NAME;
    static String ABORT_FIELD_NAME;
    static String DELETE_FIELD_NAME;
    static String SAVE_SOURCE_FIELD_NAME;

    static String METUS_SHARE_NAME;
    static String INGESTED_ASSET_DIR_NAME;

    static String COM_DIR;
    static String ASSETS_DIR;

    static String INGEST_FINISHED_DIR;
    static String INGEST_MIRRORED_DIR;

    static String TRANSFER_TODO_DIR;
    static String TRANSFER_FINISHED_DIR;
    static String TRANSFER_NEW_ASSET_DIR;
    static String TRANSFER_SMB_HOST;

    static String ENCRYPT_TODO_DIR;
    static String ENCRYPT_FINISHED_DIR;
    static String ENCRYPT_NEW_ASSET_DIR;

    static String STORAGE_TODO_DIR;
    static String STORAGE_FINISHED_DIR;
    static String STORAGE_NEW_ASSET_DIR;

    static Logger logger = LoggerFactory.getLogger(Config.class);

    public static void loadProperties(String configFilePath) {

        try (InputStream input = new FileInputStream(configFilePath)) {
            Properties properties = new Properties();
            properties.load(input);

            COM_DIR = properties.getProperty("com.dir");
            ASSETS_DIR = properties.getProperty("assets.dir");

            CUMULUS_HOST_IP = properties.getProperty("cumulus.host");
            CUMULUS_USERNAME = properties.getProperty("cumulus.username");
            CUMULUS_PASSWORD = properties.getProperty("cumulus.password");

            CUMULUS_BACKUPS_DIR = properties.getProperty("cumulus.backups.dir");
            CUMULUS_BACKUP_TRACK_FILE_DIR = COM_DIR + properties.getProperty("track.file.dir");

            CUMULUS_NUMBER_OF_CATALOGS = Integer.parseInt(properties.getProperty("cumulus.catalogs"));

            for (int i = 1; i <= CUMULUS_NUMBER_OF_CATALOGS; i++) {
                CUMULUS_CATALOG_NAMES.add(properties.getProperty("cumulus.catalog." + i + ".name"));
                CUMULUS_CATALOG_KEYS.add(properties.getProperty("cumulus.catalog." + i + ".key"));
            }

            RECORD_NAME_FIELD_NAME = properties.getProperty("cumulus.catalog.record.name.field.name");
            ID_FIELD_NAME = properties.getProperty("cumulus.catalog.id.field.name");
            INGEST_STATUS_FIELD_NAME = properties.getProperty("cumulus.catalog.ingest.status.field.name");
            ARCHIVING_STATUS_FIELD_NAME = properties.getProperty("cumulus.catalog.archiving.status.field.name");
            LR_ARCHIVING_STATUS_FIELD_NAME = properties.getProperty("cumulus.catalog.lr.archiving.status.field.name");
            HR_ARCHIVING_STATUS_FIELD_NAME = properties.getProperty("cumulus.catalog.hr.archiving.status.field.name");
            LOG_FIELD_NAME = properties.getProperty("cumulus.catalog.log.field.name");
            ASSET_REF_FIELD_NAME = properties.getProperty("cumulus.catalog.asset.ref.field.name");
            ASSET_NAME_FIELD_NAME = properties.getProperty("cumulus.catalog.asset.name.field.name");
            HIGH_RES_NAME_FIELD_NAME = properties.getProperty("cumulus.catalog.high.res.name.field.name");
            ABORT_FIELD_NAME = properties.getProperty("cumulus.catalog.abort.field.name");
            DELETE_FIELD_NAME = properties.getProperty("cumulus.catalog.delete.field.name");
            SAVE_SOURCE_FIELD_NAME = properties.getProperty("cumulus.catalog.save.source.field.name");

            METUS_SHARE_NAME = properties.getProperty("ingest.share.name");
            INGESTED_ASSET_DIR_NAME = properties.getProperty("ingest.asset.dir");

            INGEST_FINISHED_DIR = COM_DIR + properties.getProperty("ingest.finished.dir");
            INGEST_MIRRORED_DIR = COM_DIR + properties.getProperty("ingest.mirrored.dir");

            TRANSFER_TODO_DIR = COM_DIR + properties.getProperty("transfer.todo.dir");
            TRANSFER_FINISHED_DIR = COM_DIR + properties.getProperty("transfer.finished.dir");
            TRANSFER_NEW_ASSET_DIR = ASSETS_DIR + properties.getProperty("transfer.new.asset.dir");
            TRANSFER_SMB_HOST = properties.getProperty("transfer.smb.host");

            ENCRYPT_TODO_DIR = COM_DIR + properties.getProperty("encrypt.todo.dir");
            ENCRYPT_FINISHED_DIR = COM_DIR + properties.getProperty("encrypt.finished.dir");
            ENCRYPT_NEW_ASSET_DIR = ASSETS_DIR + properties.getProperty("encrypt.new.asset.dir");

            STORAGE_TODO_DIR = COM_DIR + properties.getProperty("storage.todo.dir");
            STORAGE_FINISHED_DIR = COM_DIR + properties.getProperty("storage.finished.dir");
            STORAGE_NEW_ASSET_DIR = ASSETS_DIR + properties.getProperty("storage.new.asset.dir");

        } catch (Exception e) {
            logger.error("Error when loading properties", e);
        }
    }
}
