package ikube.index.parse.excel;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.TextBox;
import com.aspose.cells.TextBoxCollection;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.aspose.cells.WorksheetCollection;

/**
 * Parser for the MS excel format.
 * 
 * @author Michael Couck
 * @since 10.09.2011
 * @version 01.00
 */
public class AsposeExcelParser implements IParser {

	protected static final Logger	LOGGER	= Logger.getLogger(AsposeExcelParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		Workbook workbook = new Workbook(inputStream);
		WorksheetCollection worksheetCollection = workbook.getWorksheets();
		for (int i = 0; i < worksheetCollection.getCount(); i++) {
			Worksheet worksheet = worksheetCollection.get(i);
			Cells cells = worksheet.getCells();
			for (int j = 0; j < cells.getCount(); j++) {
				Cell cell = cells.get(j);
				Object value = cell.getValue();
				if (value != null) {
					outputStream.write(value.toString().trim().getBytes());
					outputStream.write(' ');
				}
			}
			TextBoxCollection textBoxCollection = worksheet.getTextBoxes();
			for (int j = 0; j < textBoxCollection.getCount(); j++) {
				TextBox textBox = textBoxCollection.get(j);
				String text = textBox.getText();
				outputStream.write(text.trim().getBytes());
				outputStream.write(' ');
			}
		}
		return outputStream;
	}

}
