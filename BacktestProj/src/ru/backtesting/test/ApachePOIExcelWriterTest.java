package ru.backtesting.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableStyleInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import ru.backtesting.port.results.BacktestResultsExporter;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

public class ApachePOIExcelWriterTest {

    private static final String FILE_NAME = "\\results\\MyFirstExcel.xlsx";

    private static final String FILE_NAME_WITH_TABLE = "\\results\\backtest-result-#date#.xlsx";

    public static void main(String[] args) throws IOException {
    	testTableWithDataAdDouble();
    	
    	//testExcelExporter();
    }

    public static void testTableWithDataAdDouble() throws IOException {
		Logger.log().info("Test wrriting table with date and double columns");

    	DateTimeColumn dateColumn = DateTimeColumn.create("date", 
				Arrays.asList(new LocalDateTime[] { LocalDateTime.now() }));

		DoubleColumn balanceColumn = DoubleColumn.create("balance",
				Arrays.asList(new Double[] { new Double(10000.56) }));
		
		Table table =  Table.create("monthly returns");
			
		table.addColumns(dateColumn, balanceColumn);
		
    	dateColumn = DateTimeColumn.create("date", 
				Arrays.asList(new LocalDateTime[] { LocalDateTime.now() }));

		balanceColumn = DoubleColumn.create("balance",
				Arrays.asList(new Double[] { new Double(12000000.4675) }));
		
		table = table.append( 
					Table.create("monthly returns (loooooooooooooooooong header)").addColumns(dateColumn, balanceColumn));
		
    	BacktestResultsExporter excelExporter = new BacktestResultsExporter(LocalDateTime.now());
    	    	
		Logger.log().info(table.print(30));
		
		excelExporter.addTableSidewaysOnMainSheet("Our table with date and double", table);
		
		excelExporter.writeToFile();
    }
    
    public static void testExcelExporter() throws IOException {
    	BacktestResultsExporter excelExporter = new BacktestResultsExporter(LocalDateTime.now());
    	
    	excelExporter.addOneRowStringWithData("hello 1 string");
    	excelExporter.addOneRowStringWithData("hello 2 string");
    	
    	Table table = createDataTable();
    	
		Logger.log().info(table.print(30));
		
		excelExporter.addTableSidewaysOnMainSheet("Our first table", table);
		
		excelExporter.addTableSidewaysOnMainSheet("Our second table", table);

		excelExporter.addTableSidewaysOnMainSheet("Our third table", table);

		excelExporter.addTableSidewaysOnMainSheet("Our fourth table", table);

		
		excelExporter.writeToFile();
    }
    
    public static Table createDataTable() {
    	LocalDate[] dates = new LocalDate[] { 
				DateUtils.dateFromString("2009-12-31 00:00"),
				DateUtils.dateFromString("2010-12-31 00:00"),
				DateUtils.dateFromString("2011-12-31 00:00"),
				DateUtils.dateFromString("2012-12-31 00:00"),
				DateUtils.dateFromString("2013-12-31 00:00"),
				DateUtils.dateFromString("2014-12-31 00:00"),
				DateUtils.dateFromString("2015-12-31 00:00"),
				DateUtils.dateFromString("2016-12-31 00:00"),
				DateUtils.dateFromString("2017-12-31 00:00"),
				DateUtils.dateFromString("2018-06-06 00:00"),
				DateUtils.dateFromString("2018-12-31 00:00"),
				DateUtils.dateFromString("2009-12-31 00:00"),
				DateUtils.dateFromString("2010-12-31 00:00"),
				DateUtils.dateFromString("2011-12-31 00:00"),
				DateUtils.dateFromString("2012-12-31 00:00"),
				DateUtils.dateFromString("2013-12-31 00:00"),
				DateUtils.dateFromString("2014-12-31 00:00"),
				DateUtils.dateFromString("2015-12-31 00:00"),
				DateUtils.dateFromString("2016-12-31 00:00"),
				DateUtils.dateFromString("2017-12-31 00:00"),
				DateUtils.dateFromString("2018-06-06 00:00"),
				DateUtils.dateFromString("2018-12-31 00:00")
		};
		
		double[] drawdowns = {
				-40, -25, -21.5, -18, -12, -7, -9,     -5, -4, -18, -1, 
				-20, -12, -11.5, -9,   -6, -6, -3.5, -2.5, -2, -15, -0.5};
 				
		String[] portNames = new String[] { 
				"port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", 
				"port 2", "port 2", "port 2", "port 2"," port 2", "port 2", "port 2", "port 2", "port 2", "port 2", "port 2345"};
		
		
		Table ddTable = Table.create("Drawdowns").addColumns(
				DateColumn.create("date", dates), 
				DoubleColumn.create("drawdowns", drawdowns), 
				StringColumn.create("port name", portNames));
		
		return ddTable;
    }
    
    public static void testSimpleTable() {

    	Properties props = System.getProperties();

    	String filePath1 = props.getProperty("user.dir") + "\\logs" + FILE_NAME;

    	
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Datatypes in Java");
        Object[][] datatypes = {
                {"Datatype", "Type", "Size(in bytes)"},
                {"int", "Primitive", 2},
                {"float", "Primitive", 4},
                {"double", "Primitive", 8},
                {"char", "Primitive", 1},
                {"String", "Non-Primitive", "No fixed size"}
        };

        int rowNum = 0;
        
        System.out.println("Creating excel");

        CellStyle headerStyle = workbook.createCellStyle();
    	
    	Font dataFont = workbook.createFont();
    	dataFont.setFontHeightInPoints((short)14);
    	dataFont.setFontName("Courier New");
    	dataFont.setItalic(true);
    	dataFont.setBold(true);

    	headerStyle.setFont(dataFont);
    	headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    	headerStyle.setAlignment(HorizontalAlignment.LEFT);
    	headerStyle.setIndention((short)2);
        
        for (Object[] datatype : datatypes) {
            Row row = sheet.createRow(rowNum++);
            
            if ( rowNum == 1 )
            	row.setRowStyle(headerStyle);
            	
            int colNum = 0;
            for (Object field : datatype) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    
                    if ( rowNum == 1 )
                    	cell.setCellStyle(headerStyle);
                	
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    
                    if ( rowNum == 1 )
                    	cell.setCellStyle(headerStyle);
                	
                    cell.setCellValue((Integer) field);
                }

            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(filePath1);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Done");
        
        try {
			tableTest2();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void tableTest2() throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            
            //XSSFRow row1 = sheet.createRow(0);
            //XSSFCell cell1 = row1.createCell(0);
            //cell1.setCellValue("hello cell");
            
            int firstTableRow = 3, firstTableColum = 0;
            
            int lastTableRow = 9, lastTableColumn = 7;

            
            // Set which area the table should be placed in
            AreaReference reference = wb.getCreationHelper().createAreaReference(
                    new CellReference(firstTableRow, firstTableColum), 
                    new CellReference(lastTableRow, lastTableColumn));

            // Create
            XSSFTable table = sheet.createTable(reference);
            table.setName("BackTestResults");
            table.setDisplayName("BackTestResults");

            // For now, create the initial style in a low-level way
            table.getCTTable().addNewTableStyleInfo();
            table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

            // Style the table
            XSSFTableStyleInfo style = (XSSFTableStyleInfo) table.getStyle();
            style.setName("TableStyleMedium2");
            style.setShowColumnStripes(false);
            style.setShowRowStripes(true);
            style.setFirstColumn(false);
            style.setLastColumn(false);
            style.setShowRowStripes(true);
            style.setShowColumnStripes(true);


            
            // Set the values for the table
            XSSFRow row;
            XSSFCell cell;
            for (int rowN = firstTableRow; rowN < lastTableRow; rowN++) {
                // Create row
                row = sheet.createRow(rowN);
                for (int columnN = firstTableColum; columnN <= lastTableColumn; columnN++) {
                    // Create cell
                    cell = row.createCell(columnN);
                               
                    if (rowN == firstTableRow) {
                    	//row.setHeight((short) (35*20));
                    	//row.setHeight((short)-1);
                    	row.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));

                    	
                        cell.setCellValue("Column" + new SimpleDateFormat("yyyy-MM-dd_HH_mm").format(new Date()) + "_" + columnN);
                        cell.setCellType(CellType.STRING);
                        
                        CellStyle styleC = wb.createCellStyle();
                        styleC.setAlignment(HorizontalAlignment.LEFT);
                        styleC.setWrapText(true);
                        cell.setCellStyle(styleC);
                        
                        //row.setRowStyle(styleC);
                        
                    } else {
                        CellStyle styleDecimal = wb.createCellStyle(); // Font and alignment
                        
                        String pattern = "### ### ### ###.00";
                        
                        styleDecimal.setDataFormat(wb.createDataFormat().getFormat(pattern));
                        cell.setCellStyle(styleDecimal);
                        
                        cell.setCellType(CellType.NUMERIC);
                        
                        cell.setCellValue(new Double(rowN*100000.0 + (columnN + 100.0)/1.1).doubleValue());
                    }
                    
                    sheet.setColumnWidth(columnN, 14*256);
                }
            }
        }
    }
    
    public static void tableTest()
    	    throws FileNotFoundException, IOException
    	  {
    	Properties props = System.getProperties();

    	
    	String filePath2 = props.getProperty("user.dir") + "\\logs" + FILE_NAME_WITH_TABLE;

    	
    	   /* Read the input file that contains the data to be converted to table */
    	   FileInputStream input_document = new FileInputStream(new File(filePath2));    
    	   /* Create Workbook */
    	   XSSFWorkbook my_xlsx_workbook = new XSSFWorkbook(input_document); 
    	   /* Read worksheet */
    	   XSSFSheet sheet = my_xlsx_workbook.getSheetAt(0); 
    	   /* Create Table into Existing Worksheet */
    	   XSSFTable my_table = sheet.createTable();    
    	   /* get CTTable object*/
    	   CTTable cttable = my_table.getCTTable();
    	   /* Define Styles */    
    	   CTTableStyleInfo table_style = cttable.addNewTableStyleInfo();
    	   table_style.setName("TableStyleMedium9");           
    	   /* Define Style Options */
    	   table_style.setShowColumnStripes(false); //showColumnStripes=0
    	   table_style.setShowRowStripes(true); //showRowStripes=1    
    	   /* Define the data range including headers */
    	   AreaReference my_data_range = new AreaReference(new CellReference(0, 0), new CellReference(5, 2), null);    
    	   /* Set Range to the Table */
    	   cttable.setRef(my_data_range.formatAsString());
    	   cttable.setDisplayName("MYTABLE");      /* this is the display name of the table */
    	   cttable.setName("Test");    /* This maps to "displayName" attribute in &lt;table&gt;, OOXML */            
    	   cttable.setId(1L); //id attribute against table as long value
    	   /* Add header columns */               
    	   CTTableColumns columns = cttable.addNewTableColumns();
    	   columns.setCount(3L); //define number of columns
    	   /* Define Header Information for the Table */
    	    for (int i = 0; i < 3; i++)
    	    {
    	    CTTableColumn column = columns.addNewTableColumn();   
    	    column.setName("Column" + i);      
    	        column.setId(i+1);
    	    }   
    	    /* Write output as File */
    	    FileOutputStream fileOut = new FileOutputStream("Excel_Format_As_Table.xlsx");
    	    my_xlsx_workbook.write(fileOut);
    	    fileOut.close();
    	  }
}
