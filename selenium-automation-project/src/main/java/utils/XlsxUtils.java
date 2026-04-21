package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for reading .xlsx (Excel) files.
 *
 * <p>Uses Apache POI to extract sheet data as a list of rows, where each row
 * is represented as a {@code Map<String, String>} keyed by the column header
 * from the first non-empty row of the sheet.</p>
 *
 * <p>Usage:
 * <pre>
 *   List&lt;Map&lt;String, String&gt;&gt; rows = XlsxUtils.readSheet(filePath, "Settlement Summary");
 * </pre>
 */
public class XlsxUtils {

    /**
     * Reads a sheet by name from an xlsx file.
     *
     * @param filePath  Absolute path to the .xlsx file.
     * @param sheetName Exact name of the sheet tab (case-sensitive).
     * @return List of rows; each row is a Map of {header → cell value}.
     *         Returns an empty list if the sheet is not found or has no data.
     */
    public static List<Map<String, String>> readSheet(String filePath, String sheetName) {
        List<Map<String, String>> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                System.err.println("[XlsxUtils] Sheet NOT FOUND: \"" + sheetName + "\" in " + filePath);
                System.err.println("[XlsxUtils] Available sheets:");
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    System.err.println("    [" + i + "] \"" + workbook.getSheetName(i) + "\"");
                }
                return result;
            }

            result = extractRows(sheet);
            System.out.println("[XlsxUtils] Sheet \"" + sheetName + "\" read: " + result.size() + " data rows.");

        } catch (IOException e) {
            System.err.println("[XlsxUtils] Failed to read xlsx file: " + e.getMessage());
        }

        return result;
    }

    /**
     * Reads a sheet by its zero-based index from an xlsx file.
     * Useful as a fallback when the sheet name is unknown.
     *
     * @param filePath   Absolute path to the .xlsx file.
     * @param sheetIndex Zero-based index of the sheet.
     * @return List of rows; each row is a Map of {header → cell value}.
     */
    public static List<Map<String, String>> readSheetByIndex(String filePath, int sheetIndex) {
        List<Map<String, String>> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            if (sheetIndex >= workbook.getNumberOfSheets()) {
                System.err.println("[XlsxUtils] Sheet index " + sheetIndex + " does not exist. Total sheets: "
                        + workbook.getNumberOfSheets());
                return result;
            }

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            String actualName = workbook.getSheetName(sheetIndex);
            result = extractRows(sheet);
            System.out.println("[XlsxUtils] Sheet[" + sheetIndex + "] \"" + actualName
                    + "\" read: " + result.size() + " data rows.");

        } catch (IOException e) {
            System.err.println("[XlsxUtils] Failed to read xlsx file: " + e.getMessage());
        }

        return result;
    }

    /**
     * Lists all sheet names in an xlsx file. Useful for debugging sheet names.
     *
     * @param filePath Absolute path to the .xlsx file.
     * @return List of sheet names in order.
     */
    public static List<String> listSheetNames(String filePath) {
        List<String> names = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                names.add(workbook.getSheetName(i));
            }
        } catch (IOException e) {
            System.err.println("[XlsxUtils] Failed to list sheet names: " + e.getMessage());
        }
        return names;
    }

    // ------------------------------------------------------------------
    // Internal helper
    // ------------------------------------------------------------------

    /**
     * Extracts all rows from a sheet.
     * The first non-empty row is treated as the header row.
     * Subsequent rows are turned into Maps keyed by the header values.
     */
    private static List<Map<String, String>> extractRows(Sheet sheet) {
        List<Map<String, String>> result = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        List<String> headers = new ArrayList<>();
        boolean headersFound = false;

        for (Row row : sheet) {
            // Collect all cell values in this row
            List<String> cellValues = new ArrayList<>();
            int lastCol = row.getLastCellNum();
            for (int c = 0; c < lastCol; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValues.add(formatter.formatCellValue(cell).trim());
            }

            // Skip entirely blank rows
            boolean allBlank = cellValues.stream().allMatch(String::isEmpty);
            if (allBlank) continue;

            if (!headersFound) {
                // First non-blank row → treat as header
                headers = cellValues.stream()
                        .map(h -> h.replaceAll("\\s+", " ").trim())
                        .collect(Collectors.toList());
                headersFound = true;
            } else {
                // Data row → build Map
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c).isEmpty() ? "Column_" + c : headers.get(c);
                    String value = (c < cellValues.size()) ? cellValues.get(c) : "";
                    rowMap.put(header, value);
                }
                result.add(rowMap);
            }
        }

        return result;
    }
}
