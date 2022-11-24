package se.sunstone.msa;

import com.canto.cumulus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class CatalogHandler {
    Catalog cumCatalog;

    String key;

    GUID recordNameField;
    GUID idField;
    GUID ingestStatusField;
    GUID archivingStatusField;
    GUID lrArchivingStatusField;
    GUID hrArchivingStatusField;
    GUID logField;
    GUID assetRefField;
    GUID assetNameField;
    GUID highResNameField;
    GUID abortField;
    GUID deleteField;
    GUID archiveIdField;
    GUID highResArchiveIdField;
    GUID assetHashField;
    GUID highResAssetHashField;
    GUID saveSourceField;

    Logger logger = LoggerFactory.getLogger(CatalogHandler.class);

    public CatalogHandler(Catalog catalog, String catalogKey) {
        cumCatalog = catalog;
        key = catalogKey;

        recordNameField = getFieldGUID(Config.RECORD_NAME_FIELD_NAME);
        idField = getFieldGUID(Config.ID_FIELD_NAME);
        ingestStatusField = getFieldGUID(Config.INGEST_STATUS_FIELD_NAME);
        archivingStatusField = getFieldGUID(Config.ARCHIVING_STATUS_FIELD_NAME);
        lrArchivingStatusField = getFieldGUID(Config.LR_ARCHIVING_STATUS_FIELD_NAME);
        hrArchivingStatusField = getFieldGUID(Config.HR_ARCHIVING_STATUS_FIELD_NAME);
        logField = getFieldGUID(Config.LOG_FIELD_NAME);
        assetRefField = getFieldGUID(Config.ASSET_REF_FIELD_NAME);
        assetNameField = getFieldGUID(Config.ASSET_NAME_FIELD_NAME);
        highResNameField = getFieldGUID(Config.HIGH_RES_NAME_FIELD_NAME);
        abortField = getFieldGUID(Config.ABORT_FIELD_NAME);
        deleteField = getFieldGUID(Config.DELETE_FIELD_NAME);
        saveSourceField = getFieldGUID(Config.SAVE_SOURCE_FIELD_NAME);
    }

    private GUID getFieldGUID(String fieldName) {
        Layout layout = cumCatalog.getLayout(Cumulus.TABLE_NAME_ASSET_RECORDS);
        Set<GUID> fieldUIDs = layout.getFieldUIDs();

        for (GUID guid : fieldUIDs) {
            FieldDefinition fieldDefinition = layout.getFieldDefinition(guid);
            if (fieldDefinition.getName().equals(fieldName)) {
                return guid;
            }
        }
        logger.error(cumCatalog.getName() + ": " + fieldName + " field not found.");
        return null;
    }
}
