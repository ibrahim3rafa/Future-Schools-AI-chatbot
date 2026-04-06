package utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelUtil {

    private Workbook workbook;
    private Sheet sheet;
    private String outputFilePath;

    public ExcelUtil(String resourceFileName) throws IOException {

        // 1️⃣ Read from resources
        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(resourceFileName);

        if (is == null) {
            throw new FileNotFoundException("Excel file not found in resources: " + resourceFileName);
        }

        workbook = new XSSFWorkbook(is);
        sheet = workbook.getSheetAt(0);

        // 2️⃣ Prepare output file (outside resources)
        outputFilePath = "target/" + resourceFileName;
    }

    public int getRowCount() {
        return sheet.getLastRowNum();
    }

    public String getQuestion(int rowIndex) {
        return sheet.getRow(rowIndex)
                .getCell(0)
                .getStringCellValue();
    }


    public void writeAnswer(int rowIndex, String answer) throws IOException {
        // 1. READ: Open the existing file explicitly
        FileInputStream fis = new FileInputStream(outputFilePath);
        Workbook workbook = new XSSFWorkbook(fis); // Load the existing data
        Sheet sheet = workbook.getSheetAt(0);      // Get the first sheet (or use getSheet("Name"))

        // Close input stream immediately after loading to release the file lock
        fis.close();

        // 2. EDIT: Handle the Row and Cell updates
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        // Get cell at column 1 (B), create if missing
        Cell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(answer);

        // 3. WRITE: Save the updated workbook back to the file
        FileOutputStream fos = new FileOutputStream(outputFilePath);
        workbook.write(fos);

        // 4. CLEANUP: Close resources
        fos.close();
        workbook.close();
    }

}

