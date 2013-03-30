package ikube.action.index.parse;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;

/**
 * Parser and extractor for the PowerPoint format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class PowerPointParser implements IParser, POIFSReaderListener {

	/** The output stream for the parsed data. */
	private transient final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		POIFSReader reader = new POIFSReader();
		reader.registerListener(this);
		reader.read(inputStream);
		outputStream.write(byteArrayOutputStream.toString().getBytes());
		return outputStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void processPOIFSReaderEvent(final POIFSReaderEvent event) {
		if (!event.getName().equalsIgnoreCase("PowerPoint Document")) {
			return;
		}
		try {
			DocumentInputStream input = event.getStream();
			byte buffer[] = new byte[input.available()];
			input.read(buffer, 0, input.available());
			for (int i = 0; i < buffer.length - 20; i++) {
				long type = LittleEndian.getUShort(buffer, i + 2);
				long size = LittleEndian.getUInt(buffer, i + 4);
				if (type == 4008L) {
					byteArrayOutputStream.write(32);
					byteArrayOutputStream.write(buffer, i + 4 + 4, (int) size);
					i = (i + 4 + 4 + (int) size) - 1;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
