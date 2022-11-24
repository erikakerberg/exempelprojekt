package se.sunstone.msa;

import com.canto.cumulus.CumulusSession;
import com.canto.cumulus.Item;
import com.canto.cumulus.fieldvalue.AssetReference;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemHandler {

    final private Item cumItem;
    CatalogHandler catalog;

    public ItemHandler(Item cumItm) {
        cumItem = cumItm;
        catalog = CumConnection.catalogs.get(CumConnection.catalogIds.indexOf(cumItem.getCatalogID()));
    }

    public String getRecordName() {
        return cumItem.getStringValue(catalog.recordNameField);
    }

    public String getId() {
        return cumItem.getStringValue(catalog.idField);
    }

    public String getAssetName() {
        return cumItem.getStringValue(catalog.assetNameField);
    }

    public String getIngestStatus() {
        return cumItem.getStringValue(catalog.ingestStatusField);
    }

    public String getArchivingStatus() {
        return cumItem.getStringValue(catalog.archivingStatusField);
    }

    public String getLrArchivingStatus() {
        return cumItem.getStringValue(catalog.lrArchivingStatusField);
    }

    public String getHrArchivingStatus() {
        return cumItem.getStringValue(catalog.hrArchivingStatusField);
    }

    public String getDisplayString() {
        return getId();
    }

    public String getHighResName() {
        return cumItem.getStringValue(catalog.highResNameField);
    }

    public boolean getAborted() {
        return cumItem.getBooleanValue(catalog.abortField);
    }

    public boolean getSaveSource() {
        return cumItem.getBooleanValue(catalog.saveSourceField);
    }

    public boolean hasHrAsset() {return !getHighResName().equals("N/A");}

    public void setIngestStatus(String status) {
        cumItem.setStringValue(catalog.ingestStatusField, status);
    }

    public void setArchivingStatus(String status) {
        cumItem.setStringValue(catalog.archivingStatusField, status);
    }

    public void setLrArchivingStatus(String status) {
        cumItem.setStringValue(catalog.lrArchivingStatusField, status);
    }

    public void setHrArchivingStatus(String status) {
        cumItem.setStringValue(catalog.hrArchivingStatusField, status);
    }

    public void updateAssetRef(String assetDir, String assetName, String highResName) {
        AssetReference oldAssetReference = cumItem.getAssetReferenceValue(catalog.assetRefField);
        CumulusSession session = oldAssetReference.getCumulusSession();

        cumItem.setStringValue(catalog.assetNameField, assetName);
        cumItem.setStringValue(catalog.highResNameField, highResName);

        String assetPath = assetDir + assetName;

        AssetReference newAssetReference = new AssetReference(session, assetPath, "Standard");

        cumItem.setAssetReferenceValue(catalog.assetRefField, newAssetReference);
    }

    public void updateMetadataFromAsset() {
        cumItem.update(
                null,
                null,
                true,
                true,
                true,
                null
        );
    }

    public String parseAssetDirFromAssetRef() {
        String assetRefString = cumItem.getAssetReferenceValue(catalog.assetRefField).getDisplayString();
        String dirString = assetRefString.replace("Windows: ", "");
        dirString = dirString.replace("Windows (", "");
        dirString = dirString.replace(")", "");
        dirString = dirString.substring(0, dirString.length() - cumItem.getStringValue(catalog.assetNameField).length());

        return dirString;
    }

    public void log(String entry) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyy hh:mm:ss: ");
        LocalDateTime now = LocalDateTime.now();

        String oldLogs = "";

        try {
            oldLogs = cumItem.getStringValue(catalog.logField);
        } catch (Exception e) {
            //Do nothing
        }

        cumItem.setStringValue(catalog.logField, oldLogs + dtf.format(now) + entry + "\n");
        cumItem.save();
    }

    public void abort(String process) {
        setIngestStatus(process + "_aborted");
        setArchivingStatus(process + "_aborted");
        log(process + " aborted.");
    }

    public void deleteOriginalAsset() {
        if (getSaveSource()) {
            String assetDir = parseAssetDirFromAssetRef();

            File assetFile = new File(
                    assetDir,
                    getAssetName()
            );

            if (assetFile.exists()) {
                assetFile.delete();
                log("Deleted " + getAssetName() + " in " + assetDir);
            }

            if (!getHighResName().equals("N/A")) {

                File highResFile = new File(
                        assetDir,
                        getHighResName()
                );

                if (highResFile.exists()) {
                    highResFile.delete();
                    log("Deleted " + getHighResName() + " in " + assetDir);
                }
            }

        }
    }

    public void deleteEncryptedFiles() {
        String encryptedAssetName = getAssetName() + ".gpg";
        String highResName = getHighResName();

        File encryptedAssetFile = new File(
                Config.ENCRYPT_NEW_ASSET_DIR,
                encryptedAssetName
        );

        if (encryptedAssetFile.exists()) {
            encryptedAssetFile.delete();
            log("Deleted " + encryptedAssetName);
        }

        if (!highResName.equals("N/A")) {
            highResName += ".gpg";

            File encryptedHighResFile = new File(
                    Config.ENCRYPT_NEW_ASSET_DIR,
                    highResName
            );

            if (encryptedHighResFile.exists()) {
                encryptedHighResFile.delete();
                log("Deleted " + highResName);
            }
        }
    }

    public boolean deleteWorkingFiles() {
        String assetName;
        String highResName;
        String assetDir;

        switch (getIngestStatus() + "," + getArchivingStatus()) {
            case "Approval_aborted,Approval_aborted":
                assetName = getAssetName();
                highResName = getHighResName();
                assetDir = Config.TRANSFER_NEW_ASSET_DIR;
                break;
            case "Transferred,Waiting":
            case "N/A,Encryption_failed":
            case "Finished,Encryption_failed":
            case "Encryption_aborted,Encryption_aborted":
                assetName = getAssetName();
                highResName = getHighResName();
                assetDir = parseAssetDirFromAssetRef();
                break;
            case "N/A,Storage_failed":
            case "Finished,Storage_failed":
            case "Storage_aborted,Storage_aborted":
                assetName = getAssetName();
                highResName = getHighResName();
                assetDir = parseAssetDirFromAssetRef();
                break;
            default:
                return false;
        }

        if (assetName != null) {

            File assetFile = new File(
                    assetDir,
                    assetName
            );

            assetFile.delete();

            log("Deleted " + assetName + " in " + assetDir);

            if (!highResName.equals("N/A")) {
                File highResFile = new File(
                        assetDir,
                        getHighResName()
                );

                highResFile.delete();

                log("Deleted " + highResName + " in " + assetDir);
            }
        }

        setIngestStatus("Deleted");
        setArchivingStatus("Deleted");
        return true;
    }

    public void save() {
        cumItem.save();
    }
}
