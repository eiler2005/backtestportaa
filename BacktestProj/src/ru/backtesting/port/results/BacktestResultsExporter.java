package ru.backtesting.port.results;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Removal;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.apache.poi.xssf.usermodel.XSSFTableStyleInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;
import tech.tablesaw.api.Table;

public class BacktestResultsExporter {
	private static final String FILE_NAME_WITH_TABLE = "\\results\\backtest-result-#date#.xlsx";

	private XSSFWorkbook workbook;
	private XSSFSheet sheetMain, sheetDetailed;
	
	private int rowNIterator1 = 0, columnNIterator1 = 0;
	private int rowNIterator2 = 0, columnNIterator2 = 0;;

	
	private LocalDateTime launchDate;

	public BacktestResultsExporter(LocalDateTime launchDate) {
		workbook = new XSSFWorkbook();
		sheetMain = workbook.createSheet("main");

		sheetDetailed = workbook.createSheet("detailed");

		this.launchDate = launchDate;
	}

	// First header
	public void addSheetHeader(String data) {
		int firstRow = rowNIterator1 + 1;
		int lastRow = rowNIterator1 + 1;
		int firstCol = columnNIterator1;
		int lastCol = 6;

		XSSFRow row;
		if (!isFirsTableChange1())
			row = sheetMain.getRow(firstRow);
		else
			row = sheetMain.createRow(firstRow);

		XSSFCell cell = row.createCell(firstCol);

		cell.setCellValue(data);

		cell.setCellType(CellType.STRING);

		CellStyle styleC = workbook.createCellStyle();
		styleC.setAlignment(HorizontalAlignment.LEFT);
		styleC.setWrapText(true);

		Font font = workbook.createFont();
		font.setFontName("Calibri");
		font.setBold(true);
		font.setFontHeight((short) (14 * 20));

		styleC.setFont(font);

		cell.setCellStyle(styleC);

		sheetMain.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

		rowNIterator1++;
	}

	public void addPortParams(Map<String, Object> params, String header) {
		Logger.log().trace("Помещаем в excel-файл результатов бектеста блок данных с заголовком: " + header);

		addOneRowStringWithData(header);

		for (String param : params.keySet()) {
			Logger.log().trace("Значение параметра: " + param + ", value: " + params.get(param));

			if ( params.containsKey(param) && params.get(param) != null )
				addRowWithData(param, params.get(param));
			else
				addRowWithData(param, "");

		}
	}

	public void addOneRowStringWithData(String data) {
		int firstRow = rowNIterator1 + 2;
		int lastRow = rowNIterator1 + 2;
		int firstCol = columnNIterator1;
		int lastCol = 6;

		XSSFRow row = sheetMain.createRow(firstRow);

		XSSFCell cell = row.createCell(firstCol);

		cell.setCellValue(data);

		cell.setCellType(CellType.STRING);

		CellStyle styleC = workbook.createCellStyle();
		styleC.setAlignment(HorizontalAlignment.LEFT);

		Font font = workbook.createFont();
		font.setFontName("Calibri");
		font.setBold(true);
		font.setFontHeight((short) (12 * 20));

		styleC.setFont(font);

		cell.setCellStyle(styleC);

		row.setRowStyle(styleC);

		sheetMain.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

		rowNIterator1 += 2;
	}

	public void addRowWithData(String paramName, Object data) {
		int firstRow = rowNIterator1 + 1;
		int lastRow = rowNIterator1 + 1;
		int firstCol = columnNIterator1;
		int lastCol = 2;

		XSSFRow row = sheetMain.createRow(firstRow);

		XSSFCell cellParamName = row.createCell(firstCol);

		row.createCell(rowNIterator1 + 1);
		row.createCell(rowNIterator1 + 2);
		row.createCell(rowNIterator1 + 3);

		XSSFCell cellPAramValue = row.createCell(4);

		addCellValue(cellParamName, paramName);
		addCellValue(cellPAramValue, data);
		
		CellStyle styleC = workbook.createCellStyle();
		styleC.setAlignment(HorizontalAlignment.CENTER);

		cellPAramValue.setCellStyle(styleC);

		sheetMain.setColumnWidth(firstCol, 14 * 256);
		sheetMain.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));

		rowNIterator1++;
	}

	private void addCellValue(Cell cell, Object value) {
		CellStyle cellStyle = workbook.createCellStyle();

		if (value instanceof Double) {

			String pattern = "#,##0.00";

			// String pattern = "###\\ ###\\ ###,##";

			cellStyle.setDataFormat(workbook.createDataFormat().getFormat(pattern));
			cellStyle.setAlignment(HorizontalAlignment.CENTER);

			Logger.log().trace("Добавляем в ячейку таблицы данные с форматом double: " + value);

			cell.setCellValue(((Double) value).doubleValue());
		} else if (value instanceof Boolean) {
			boolean valueB = ((Boolean) value).booleanValue();

			if (valueB)
				cell.setCellValue("on");
			else
				cell.setCellValue("off");

			cellStyle.setAlignment(HorizontalAlignment.CENTER);

		} else if (value instanceof String) {
			cell.setCellValue(((String) value));
		} else if (value instanceof LocalDateTime) {
			CreationHelper creationHelper = workbook.getCreationHelper();
			DataFormat dataFormat = creationHelper.createDataFormat();
			short fmt = dataFormat.getFormat("yyyy-mm-dd");
			cellStyle.setDataFormat(fmt);

			cellStyle.setAlignment(HorizontalAlignment.LEFT);

			cell.setCellValue((((LocalDateTime) value)).toLocalDate());

			Logger.log().trace("Добавляем в ячейку таблицы данные с форматом date: " + value);

		} else {
			cellStyle.setAlignment(HorizontalAlignment.CENTER);

			cell.setCellValue(value.toString());
		}

		cell.setCellStyle(cellStyle);

	}

	public boolean isFirsTableChange1() {
		return (columnNIterator1 == 0);
	}
	
	public boolean isFirsTableChange2() {
		return (columnNIterator2 == 0);
	}

	public void addTableSidewaysOnMainSheet(String tableHeader, Table tableData) {
		int topLeftRowN = rowNIterator1 + 3;

		int topLeftColumnN = 0;

		if (!isFirsTableChange1())
			topLeftColumnN = columnNIterator1 + 2;

		int bottomRightRowN = topLeftRowN + tableData.rowCount();

		int bottomRightColumnN = topLeftColumnN + tableData.columnCount() - 1;

		Logger.log().trace("addTableSidewaysOnMainSheet method tracing");

		// table header
		addTableHeader1(tableHeader, topLeftRowN - 1, topLeftColumnN, bottomRightColumnN, sheetMain);

		Logger.log().trace("curRowN = " + rowNIterator1);
		Logger.log().trace("curColumnN = " + columnNIterator1);
		Logger.log().trace("firstTableRowN = " + topLeftRowN);
		Logger.log().trace("firstTableColumnN = " + topLeftColumnN);
		Logger.log().trace("lastTableRowN = " + bottomRightRowN);
		Logger.log().trace("lastTableColumnN = " + bottomRightColumnN);

		XSSFRow header;

		if (!isFirsTableChange1())
			header = sheetMain.getRow(topLeftRowN);
		else
			header = sheetMain.createRow(topLeftRowN);

		for (int i = 0; i < tableData.columnNames().size(); i++) {
			int headerColumnN = topLeftColumnN + i;

			XSSFCell headerCell = header.createCell(headerColumnN);

			// string
			headerCell.setCellValue(tableData.columnNames().get(i));

			CellStyle styleC = workbook.createCellStyle();
			styleC.setAlignment(HorizontalAlignment.CENTER);
			styleC.setWrapText(true);

			headerCell.setCellStyle(styleC);

			Logger.log().trace("create table header cell with indexes [" + topLeftRowN + ", " + headerColumnN
					+ "] + value = " + tableData.columnNames().get(i));
		}

		int columnN = 0;

		for (int i = 0; i < tableData.rowCount(); i++) {
			int rowN = topLeftRowN + 1 + i;

			XSSFRow row;

			if (!isFirsTableChange1())
				row = sheetMain.getRow(rowN);
			else
				row = sheetMain.createRow(rowN);

			for (int j = 0; j < tableData.columnCount(); j++) {
				columnN = topLeftColumnN + j;

				// Create cell
				XSSFCell cell = row.createCell(columnN);

				Object value = tableData.get(i, j);

				addTableHeaderCellValue(topLeftRowN, rowN, row, cell, value);

				// sheetMain.setColumnWidth(columnN, 14 * 256);

				sheetMain.autoSizeColumn(columnN, true);

				Logger.log().trace("create cell with indexes [" + rowN + ", " + columnN + "] + value = " + value);
			}
		}

		columnNIterator1 = columnN;

		Logger.log().trace("curRowN = " + rowNIterator1);
		Logger.log().trace("curColumnN = " + columnNIterator1);

		createBaseTable(topLeftRowN, topLeftColumnN, bottomRightRowN, bottomRightColumnN, sheetMain);
	}

	public void addTableSidewaysOnDetailedSheet(String tableHeader, Table tableData) {
		int topLeftRowN = rowNIterator2 + 3;

		int topLeftColumnN = 0;

		if (!isFirsTableChange2())
			topLeftColumnN = columnNIterator2 + 2;

		int bottomRightRowN = topLeftRowN + tableData.rowCount();

		int bottomRightColumnN = topLeftColumnN + tableData.columnCount() - 1;

		Logger.log().trace("addTableSidewaysOnDetailedSheet method tracing");

		// table header
		addTableHeader2(tableHeader, topLeftRowN - 1, topLeftColumnN, bottomRightColumnN, sheetDetailed);

		Logger.log().trace("curRowN = " + rowNIterator1);
		Logger.log().trace("curColumnN = " + columnNIterator1);
		Logger.log().trace("firstTableRowN = " + topLeftRowN);
		Logger.log().trace("firstTableColumnN = " + topLeftColumnN);
		Logger.log().trace("lastTableRowN = " + bottomRightRowN);
		Logger.log().trace("lastTableColumnN = " + bottomRightColumnN);

		XSSFRow header;

		if (!isFirsTableChange2())
			header = sheetDetailed.getRow(topLeftRowN);
		else
			header = sheetDetailed.createRow(topLeftRowN);

		for (int i = 0; i < tableData.columnNames().size(); i++) {
			int headerColumnN = topLeftColumnN + i;

			XSSFCell headerCell = header.createCell(headerColumnN);

			// string
			headerCell.setCellValue(tableData.columnNames().get(i));

			CellStyle styleC = workbook.createCellStyle();
			styleC.setAlignment(HorizontalAlignment.CENTER);
			styleC.setWrapText(true);

			headerCell.setCellStyle(styleC);

			Logger.log().trace("create table header cell with indexes [" + topLeftRowN + ", " + headerColumnN
					+ "] + value = " + tableData.columnNames().get(i));
		}

		int columnN = 0;

		for (int i = 0; i < tableData.rowCount(); i++) {
			int rowN = topLeftRowN + 1 + i;

			XSSFRow row;

			if (!isFirsTableChange2())
				row = sheetDetailed.getRow(rowN);
			else
				row = sheetDetailed.createRow(rowN);

			for (int j = 0; j < tableData.columnCount(); j++) {
				columnN = topLeftColumnN + j;

				// Create cell
				XSSFCell cell = row.createCell(columnN);

				Object value = tableData.get(i, j);

				addTableHeaderCellValue(topLeftRowN, rowN, row, cell, value);

				// sheetMain.setColumnWidth(columnN, 14 * 256);

				sheetDetailed.autoSizeColumn(columnN);

				Logger.log().trace("create cell with indexes [" + rowN + ", " + columnN + "] + value = " + value);
			}
		}

		columnNIterator2 = columnN;

		Logger.log().trace("curRowN = " + rowNIterator2);
		Logger.log().trace("curColumnN = " + columnNIterator2);

		createBaseTable(topLeftRowN, topLeftColumnN, bottomRightRowN, bottomRightColumnN, sheetDetailed);
	}
	
	private void addTableHeader1(String headerData, int firstTableRow, int firstTableColum, int lastTableColumn, XSSFSheet sheet) {
		int headerRowN = firstTableRow;

		XSSFRow header;

		if (!isFirsTableChange1())
			header = sheet.getRow(headerRowN);
		else
			header = sheet.createRow(headerRowN);

		XSSFCell headerCell = header.createCell(firstTableColum);

		Font font = workbook.createFont();
		font.setFontName("Arial");
		font.setBold(true);
		font.setFontHeight((short) (11 * 20));

		header.setHeight((short) (29 * 20));
		
		CellStyle styleC = workbook.createCellStyle();
		styleC.setAlignment(HorizontalAlignment.LEFT);
		styleC.setWrapText(true);

		styleC.setFont(font);

		headerCell.setCellStyle(styleC);

		headerCell.setCellValue(headerData);
		
		Logger.log().trace("create header of table cell with indexes [" + headerRowN + ", " + firstTableColum
				+ "] + value = " + headerData);

		sheet.addMergedRegion(new CellRangeAddress(headerRowN, headerRowN, firstTableColum, lastTableColumn));		
	}

	private void addTableHeader2(String headerData, int firstTableRow, int firstTableColum, int lastTableColumn, XSSFSheet sheet) {
		int headerRowN = firstTableRow;

		XSSFRow header;

		if (!isFirsTableChange2())
			header = sheet.getRow(headerRowN);
		else
			header = sheet.createRow(headerRowN);

		XSSFCell headerCell = header.createCell(firstTableColum);

		Font font = workbook.createFont();
		font.setFontName("Arial");
		font.setBold(true);
		font.setFontHeight((short) (11 * 20));

		header.setHeight((short) (29 * 20));
		
		
		CellStyle styleC = workbook.createCellStyle();
		styleC.setAlignment(HorizontalAlignment.LEFT);
		styleC.setWrapText(true);

		styleC.setFont(font);

		headerCell.setCellStyle(styleC);

		headerCell.setCellValue(headerData);

		Logger.log().trace("create header of table cell with indexes [" + headerRowN + ", " + firstTableColum
				+ "] + value = " + headerData);

		sheet.addMergedRegion(new CellRangeAddress(headerRowN, headerRowN, firstTableColum, lastTableColumn));
	}
	
	private void addTableHeaderCellValue(int firstTableRow, int rowN, XSSFRow row, XSSFCell cell, Object value) {
		// header of table
		if (rowN == firstTableRow) {
			row.setHeightInPoints((2 * sheetMain.getDefaultRowHeightInPoints()));

			cell.setCellValue(value.toString());
			cell.setCellType(CellType.STRING);

			CellStyle styleC = workbook.createCellStyle();
			styleC.setAlignment(HorizontalAlignment.LEFT);
			styleC.setWrapText(true);
			cell.setCellStyle(styleC);
		} else {
			addCellValue(cell, value);
		}
	}

	public void writeToFile() throws IOException {
		Properties props = System.getProperties();

		String curDateStr = new SimpleDateFormat("yyyy-MM-dd_HH_mm").format(DateUtils.asDate(launchDate));

		String filePath = (props.getProperty("user.dir") + "\\logs" + FILE_NAME_WITH_TABLE).replaceAll("#date#",
				curDateStr);

		try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
			workbook.write(fileOut);
		}
	}

	private XSSFTable createBaseTable(int topLeftRowN, int topLeftColumnN, int bottomRightRowN, int bottomRightColumnN,
			XSSFSheet sheet) {
		// Create table
		// Set which area the table should be placed in
		AreaReference reference = workbook.getCreationHelper().createAreaReference(
				new CellReference(sheet.getRow(topLeftRowN).getCell(topLeftColumnN)),
				new CellReference(sheet.getRow(bottomRightRowN).getCell(bottomRightColumnN)));

		XSSFTable tableExcel = sheet.createTable(reference);

		// For now, create the initial style in a low-level way
		tableExcel.getCTTable().addNewTableStyleInfo();
		tableExcel.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

		// Style the table
		XSSFTableStyleInfo style = (XSSFTableStyleInfo) tableExcel.getStyle();
		style.setName("TableStyleMedium2");
		style.setShowColumnStripes(false);
		style.setShowRowStripes(true);
		style.setFirstColumn(false);
		style.setLastColumn(false);
		style.setShowRowStripes(true);
		style.setShowColumnStripes(true);

		Logger.log().trace("table area [" + topLeftRowN + ", " + topLeftColumnN + "], [ " + bottomRightRowN + ", "
				+ bottomRightColumnN + "]");

		Logger.log().trace("reference.getFirstCell() = [" + reference.getFirstCell().getRow() + ", "
				+ reference.getFirstCell().getCol() + "]");
		Logger.log().trace("reference.getLastCell() = [" + reference.getLastCell().getRow() + ", "
				+ reference.getLastCell().getCol() + "]");

		Logger.log().trace("tableExcel.getColumnCount() = " + tableExcel.getColumnCount());
		Logger.log().trace("tableExcel.getColumns() = " + tableExcel.getColumns().toString());

		for (XSSFTableColumn col : tableExcel.getColumns()) {
			Logger.log().trace(
					"tableExcel.getColumn = " + col.getName() + ", " + col.getId() + ", " + col.getColumnIndex());
		}

		return tableExcel;
	}
	
	@Deprecated
	@Removal()
	public void addTableDown(String tableHeader, Table tableData) {
		int firstTableRowN = rowNIterator1 + 3, firstTableColumnN = 0;

		int lastTableRowN = firstTableRowN + tableData.rowCount();

		int lastTableColumnN = firstTableColumnN + tableData.columnCount() - 1;

		// Set which area the table should be placed in
		AreaReference reference = workbook.getCreationHelper().createAreaReference(
				new CellReference(firstTableRowN, firstTableColumnN),
				new CellReference(lastTableRowN, lastTableColumnN));

		// table header
		addTableHeader1(tableHeader, firstTableRowN - 1, firstTableColumnN, lastTableColumnN, sheetMain);

		// Create
		XSSFTable tableExcel = sheetMain.createTable(reference);
		tableExcel.setName(tableData.name());
		tableExcel.setDisplayName(tableData.name());

		// For now, create the initial style in a low-level way
		tableExcel.getCTTable().addNewTableStyleInfo();
		tableExcel.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

		// Style the table
		XSSFTableStyleInfo style = (XSSFTableStyleInfo) tableExcel.getStyle();
		style.setName("TableStyleMedium2");
		style.setShowColumnStripes(false);
		style.setShowRowStripes(true);
		style.setFirstColumn(false);
		style.setLastColumn(false);
		style.setShowRowStripes(true);
		style.setShowColumnStripes(true);

		int rowN;
		int columnN;

		XSSFRow header = sheetMain.createRow(firstTableRowN);

		for (int i = 0; i < tableData.columnNames().size(); i++) {
			XSSFCell headerCell = header.createCell(firstTableColumnN + i);

			headerCell.setCellValue(tableData.columnNames().get(i));

			CellStyle styleC = workbook.createCellStyle();
			styleC.setAlignment(HorizontalAlignment.CENTER);
			styleC.setWrapText(true);

			headerCell.setCellStyle(styleC);
		}

		for (int i = 0; i < tableData.rowCount(); i++) {
			rowN = firstTableRowN + 1 + i;

			XSSFRow row = sheetMain.createRow(rowN);

			for (int j = 0; j < tableData.columnCount(); j++) {
				columnN = firstTableColumnN + j;

				// Create cell
				XSSFCell cell = row.createCell(columnN);

				Object value = tableData.get(i, j);

				addTableHeaderCellValue(firstTableRowN, rowN, row, cell, value);

				sheetMain.setColumnWidth(columnN, 14 * 256);
			}

			rowNIterator1 = rowN + 1;
		}

		System.out.println("curRowN " + rowNIterator1);
	}
}
