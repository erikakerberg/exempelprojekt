package se.sunstone.msa;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JobFile {

    static Logger logger = LoggerFactory.getLogger(JobFile.class);

    public static JSONObject readFinishedFile(String filename, String type) throws Exception {
        String finishedDir;

        switch(type) {
            case "transfer":
                finishedDir = Config.TRANSFER_FINISHED_DIR;
                break;
            case "encrypt":
                finishedDir = Config.ENCRYPT_FINISHED_DIR;
                break;
            case "storage":
                finishedDir = Config.STORAGE_FINISHED_DIR;
                break;
            default:
                return null;
        }

        String contentOfFinishedFile = Files.readString(Paths.get(finishedDir + filename));

        return new JSONObject(contentOfFinishedFile);
    }

    public static void deleteFinishedFile(String filename, String type) {
        String finishedDir;

        switch(type) {
            case "transfer":
                finishedDir = Config.TRANSFER_FINISHED_DIR;
                break;
            case "encrypt":
                finishedDir = Config.ENCRYPT_FINISHED_DIR;
                break;
            case "storage":
                finishedDir = Config.STORAGE_FINISHED_DIR;
                break;
            default:
                return;
        }

        File transferFinishedFile = new File(finishedDir, filename);
        transferFinishedFile.delete();
    }

    public static void createTodoFile(JSONObject todoAsJson, String fileId, String type, String assetType) {
        String todoDir;

        switch(type) {
            case "transfer":
                todoDir = Config.TRANSFER_TODO_DIR;
                break;
            case "encrypt":
                todoDir = Config.ENCRYPT_TODO_DIR;
                break;
            case "storage":
                todoDir = Config.STORAGE_TODO_DIR;
                break;
            default:
                return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hhmmss");
        LocalDateTime now = LocalDateTime.now();

        String todoFilename = type + "-" + assetType + dtf.format(now) + fileId + ".json";
        File todoFile = new File(todoDir, todoFilename);

        try {
            todoFile.createNewFile();

            String todoAsJsonText = todoAsJson.toString();

            FileWriter w = new FileWriter(todoFile);
            w.write(todoAsJsonText);
            w.close();

        } catch (Exception e) {
            logger.error("Error creating job file", e);
        }
    }
}
