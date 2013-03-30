package ikube.action.index.parse;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Parser for the MS excel format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class ExcelParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);
		HSSFWorkbook workbook = new HSSFWorkbook(fileSystem);
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			HSSFSheet sheet = workbook.getSheetAt(i);
			Iterator<?> rowIterator = sheet.rowIterator();
			while (rowIterator.hasNext()) {
				HSSFRow row = (HSSFRow) rowIterator.next();
				Iterator<?> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					HSSFCell cell = (HSSFCell) cellIterator.next();
					String text = "";
					// Numeric = 1, string = 2, formula = 3, blank = 4, boolean = 5, error = 6
					switch (cell.getCellType()) {
					case HSSFCell.CELL_TYPE_BLANK:
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						text = Boolean.toString(cell.getBooleanCellValue());
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						text = Byte.toString(cell.getErrorCellValue());
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						text = cell.getCellFormula();
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						text = Double.toString(cell.getNumericCellValue());
						break;
					case HSSFCell.CELL_TYPE_STRING:
						text = cell.toString();
						break;
					default:
						break;
					}
					outputStream.write(text.getBytes());
					outputStream.write(" ".getBytes());
				}
			}
		}
		return outputStream;
	}

}
