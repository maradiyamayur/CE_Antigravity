package utils;

import java.io.File;

/**
 * Utility class for managing file download operations during test execution.
 */
public class FileDownloadUtils {

    /**
     * Deletes all files inside the specified download folder before a new test run.
     * Handles gracefully if the folder is empty or does not exist.
     *
     * @param downloadFolderPath Absolute path to the download directory.
     */
    public static void cleanDownloadDirectory(String downloadFolderPath) {
        File folder = new File(downloadFolderPath);

        if (!folder.exists()) {
            System.out.println("[FileDownloadUtils] Download folder does not exist, skipping cleanup: " + downloadFolderPath);
            return;
        }

        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("[FileDownloadUtils] Download folder is already clean.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("[FileDownloadUtils] Deleted file: " + file.getName());
                } else {
                    System.err.println("[FileDownloadUtils] WARNING: Could not delete file: " + file.getName());
                }
            }
        }

        System.out.println("[FileDownloadUtils] Download folder cleanup complete.");
    }
}
