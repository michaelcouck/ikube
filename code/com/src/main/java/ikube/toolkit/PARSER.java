package ikube.toolkit;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-12-2015
 */
public class PARSER {

    public static String parse(final InputStream inputStream) {
        BodyContentHandler handler = new BodyContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try {
            parser.parse(inputStream, handler, metadata);
            return handler.toString();
        } catch (final IOException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
