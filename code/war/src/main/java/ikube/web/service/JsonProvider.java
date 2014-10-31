package ikube.web.service;

import com.google.gson.Gson;
import ikube.IConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This is the Json provider for Jersey. Jackson was not playing nicely, so we switched to Gson. This class
 * will convert to and from Json for the rest web services.
 * <p/>
 * Un-deprecated This was used because Jackson didn't like the inner arrays, but interferes with file upload,
 * essentially to use this in place of Jackson you have to then also write a multipart upload provider, and of
 * course the list goes on if you want other types...so back to Jackson
 *
 * @author Michael couck
 * @version 01.00
 * @since 16-08-2014
 */
@Provider
public class JsonProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
                           final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException {
        try (final InputStreamReader streamReader = new InputStreamReader(entityStream, IConstants.ENCODING)) {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            } else {
                jsonType = genericType;
            }
            return getGson().fromJson(streamReader, jsonType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(final Object object, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException {
        try (final OutputStreamWriter writer = new OutputStreamWriter(entityStream, IConstants.ENCODING)) {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            } else {
                jsonType = genericType;
            }
            getGson().toJson(object, jsonType, writer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize(final Object object, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    private Gson getGson() {
        return IConstants.GSON;
    }

}
