package ikube.index.parse;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.excel.ExcelParser;
import ikube.index.parse.html.HtmlParser;
import ikube.index.parse.pdf.PdfParser;
import ikube.index.parse.pp.PowerPointParser;
import ikube.index.parse.rtf.RtfParser;
import ikube.index.parse.text.TextParser;
import ikube.index.parse.word.MSWordParser;
import ikube.index.parse.xml.XMLParser;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ParserPerformanceTest extends ATest {

	public ParserPerformanceTest() {
		super(ParserPerformanceTest.class);
	}

	@Test
	public void pdfPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "pdf.pdf");
		final PdfParser pdfParser = new PdfParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				pdfParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Pdf parser", 100);
	}

	@Test
	public void xmlPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "xml.xml");
		final XMLParser xmlParser = new XMLParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				xmlParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Xml parser", 100);
	}

	@Test
	public void htmlPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "html.html");
		final HtmlParser htmlParser = new HtmlParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				htmlParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Html parser", 100);
	}

	@Test
	public void wordPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "doc.doc");
		final MSWordParser wordParser = new MSWordParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				wordParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Word parser", 100);
	}

	@Test
	public void textPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "txt.txt");
		final TextParser textParser = new TextParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				textParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Text parser", 100);
	}

	@Test
	public void excelPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "xls.xls");
		final ExcelParser excelParser = new ExcelParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				excelParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Excel parser", 100);
	}

	@Test
	public void ppPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "pot.pot");
		final PowerPointParser ppParser = new PowerPointParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				ppParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "PP parser", 100);
	}

	@Test
	public void rtfPerformance() throws Exception {
		final File file = FileUtilities.findFile(new File("."), "rtf.rtf");
		final RtfParser rtfParser = new RtfParser();
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				InputStream inputStream = new FileInputStream(file);
				rtfParser.parse(inputStream, new ByteArrayOutputStream());
			}
		}, "Rtf parser", 100);
	}

	@Test
	public void patternPerformance() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "html.html" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		final String string = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		// (@)?(href=')?(HREF=')?(HREF=\")?(href=\")?
		// This pattern will extract the urls form the log so we can check them
		final Pattern pattern = Pattern
				.compile("(http://|https://)[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
		double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() {
				Matcher matcher = pattern.matcher(string);
				while (matcher.find()) {
					matcher.group();
				}
			}
		}, "Pattern matcher : ", 1000);
		assertTrue(executionsPerSecond > 10);
	}

}