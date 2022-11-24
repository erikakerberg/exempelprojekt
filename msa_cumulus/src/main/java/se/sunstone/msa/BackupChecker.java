package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

public class BackupChecker {

    static Logger logger = LoggerFactory.getLogger(BackupChecker.class);

    public static void run() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        File backupsDir = new File(Config.CUMULUS_BACKUPS_DIR);
        File backupTrackFileDir = new File(Config.CUMULUS_BACKUP_TRACK_FILE_DIR);

        File last_backup_file = null;
        String last_backup_date_string = "";
        LocalDate last_backup_date = null;

        for (String filename : backupTrackFileDir.list()) {
            if (filename.contains("lastbackup")) {
                last_backup_file = new File(backupTrackFileDir, filename);
                last_backup_date_string = filename.substring(filename.length() - 10);
                last_backup_date = LocalDate.parse(last_backup_date_string);
            }
        }

        if (last_backup_date.isEqual(now)) {
            return;
        }

        for (String filename : backupsDir.list()) {
            Pattern pattern = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filename);

            if(matcher.find()) {
                String file_date_string = filename.substring(matcher.start(), matcher.end());
                LocalDate date = LocalDate.parse(file_date_string);

                if (date.isAfter(last_backup_date)) {
                    makeBackupEncryptTodo(filename, Config.CUMULUS_BACKUPS_DIR);
                    logger.info(filename + " in queue for encryption.");
                }
            }
        }

        File renamed_last_backup_file = new File(backupTrackFileDir, "lastbackup-" + dtf.format(now));
        last_backup_file.renameTo(renamed_last_backup_file);
    }

    public static void makeBackupEncryptTodo(String filename, String fileDir) {
        JSONObject todoAsJson = new JSONObject();

        try {
            todoAsJson.put("id", "BACKUPFILE");
            todoAsJson.put("assetDir", fileDir);
            todoAsJson.put("assetFilename", filename);
            todoAsJson.put("saveSource", true);
            todoAsJson.put("catalogKey", "NA");
            todoAsJson.put("assetType", "NA");
            todoAsJson.put("isBackupFile", true);
        } catch (Exception e) {
            logger.error("Error creating json file", e);
            return;
        }

        JobFile.createTodoFile(todoAsJson, "", "encrypt", "BACKUP");
    }
}
