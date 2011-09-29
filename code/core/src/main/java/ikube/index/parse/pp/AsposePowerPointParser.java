package ikube.index.parse.pp;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.aspose.slides.pptx.AutoShapeEx;
import com.aspose.slides.pptx.GroupShapeEx;
import com.aspose.slides.pptx.ParagraphEx;
import com.aspose.slides.pptx.PresentationEx;
import com.aspose.slides.pptx.ShapeEx;
import com.aspose.slides.pptx.ShapesEx;
import com.aspose.slides.pptx.SlideEx;
import com.aspose.slides.pptx.TableEx;
import com.aspose.slides.pptx.TextFrameEx;

/**
 * Parser and extractor for the PowerPoint format.
 * 
 * @author Michael Couck
 * @since 10.09.2011
 * @version 01.00
 */
public class AsposePowerPointParser implements IParser {

	/** Logger for the parser class. */
	private static final Logger	LOGGER	= Logger.getLogger(AsposePowerPointParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		getText(inputStream, outputStream);
		return outputStream;
	}

	private void getText(final InputStream inputStream, final OutputStream outputStream) {
		try {
			// Opening presentation
			PresentationEx presentation = new PresentationEx(inputStream);
			// Traversing through all slides
			SlideEx slide;
			ShapesEx shps;
			for (int index = 0; index < presentation.getSlides().size(); index++) {
				// Accessing Slides
				slide = presentation.getSlides().get(index);
				// Accessing all shapes in slide
				shps = slide.getShapes();
				ShapeEx shape;
				// Traversing through all shapes
				for (int shpCount = 0; shpCount < shps.size(); shpCount++) {
					shape = shps.get(shpCount);
					if (shape.getPlaceholder() != null) {
						try {
							if (shape instanceof AutoShapeEx) {
								// Getting AutoShape from group shapes set
								AutoShapeEx aShape = (AutoShapeEx) shape;
								if (aShape.getTextFrame() != null) {
									// Accessing the text frame of shape
									TextFrameEx tfText = aShape.getTextFrame();
									getText(tfText, outputStream);
								}// End Text Frame IF
							}
						} catch (Exception e) {
							LOGGER.error("Exception : " + e.getMessage());
						}
					}// End AutoShape Check
					else if (shape instanceof AutoShapeEx) {
						// Getting AutoShape from group shapes set
						try {
							AutoShapeEx aShp = (AutoShapeEx) shape;
							if (aShp.getTextFrame() != null) {
								// Accessing the text frame of shape
								TextFrameEx tfText = aShp.getTextFrame();
								getText(tfText, outputStream);
							}// End Text Frame IF
						} catch (Exception e) {
							LOGGER.error("Exception : " + e.getMessage());
						}
					}// End AutoShape Check
						// If shape is a group shape
					else if (shape instanceof GroupShapeEx) {
						// Type casting shape to group shape
						GroupShapeEx gShape = (GroupShapeEx) shape;
						// Traversing through all shapes in group shape
						for (int iCount = 0; iCount < gShape.getShapes().size(); iCount++) {
							if (gShape.getShapes().get(iCount) instanceof AutoShapeEx) {
								// Getting AutoShape from group shapes set
								AutoShapeEx aShp = (AutoShapeEx) gShape.getShapes().get(iCount);
								if (aShp.getTextFrame() != null) {
									TextFrameEx tfText = aShp.getTextFrame();
									getText(tfText, outputStream);
								}// End Text Frame IF
							}
						}
					}
					// If shape is instance of Table
					else if (shape instanceof TableEx) {
						TableEx tTable = (TableEx) shape;
						for (int iCol = 0; iCol < tTable.getColumns().size(); iCol++) {
							for (int iRow = 0; iRow < tTable.getRows().size(); iRow++) {
								TextFrameEx tfText = tTable.get(iCol, iRow).getTextFrame();
								if (tfText != null)
									getText(tfText, outputStream);
							}// End Row Loop
						}// End Col Loop
					}// End Group Shape IF
				}// End Shape Loop
			}// End Slide Traversal
		} catch (Exception e) {
			LOGGER.error("Exception reading power point document : ", e);
		}
	}

	private void getText(TextFrameEx TxtFrame, OutputStream outputStream) {
		for (int pgCount = 0; pgCount < TxtFrame.getParagraphs().size(); pgCount++) {
			ParagraphEx Paragraph = TxtFrame.getParagraphs().get(pgCount);
			for (int prCount = 0; prCount < Paragraph.getPortions().size(); prCount++) {
				try {
					String prText = Paragraph.getPortions().get(prCount).getText();
					outputStream.write(prText.trim().getBytes());
					outputStream.write(' ');
				} catch (Exception e) {
					LOGGER.error("Exception extracting the text from the document : ", e);
				}
			}// End Portion Loop
		}// End Paragraphs Loop
	}

}