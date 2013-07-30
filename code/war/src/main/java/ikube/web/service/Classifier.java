package ikube.web.service;

import ikube.IConstants;
import ikube.action.index.handler.strategy.ClassificationStrategy;
import ikube.action.index.handler.strategy.LanguageCleaningStrategy;
import ikube.action.index.handler.strategy.LanguageDetectionStrategy;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Michael couck
 * @since 02.07.13
 * @version 01.00
 */
@Component
@Path(Classifier.CLASSIFIER)
@Scope(Resource.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Classifier extends Resource {

	public static final String CLASSIFY = "/classify";
	public static final String CLASSIFIER = "/classifier/json";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Classifier.class);

	@Autowired
	private LanguageCleaningStrategy languageCleaningStrategy;
	@Autowired
	private LanguageDetectionStrategy languageDetectionStrategy;
	@Autowired
	private ClassificationStrategy classificationStrategy;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@GET
	@Path(Classifier.CLASSIFY)
	@Consumes(MediaType.APPLICATION_XML)
	public Response classify(@QueryParam(value = IConstants.CONTENT) final String content) throws IOException {
		String cleanedContent = languageCleaningStrategy.cleanContent(content);
		String language = languageDetectionStrategy.detectLanguage(cleanedContent);
		if (language != null) {
			String sentiment = classificationStrategy.detectSentiment(cleanedContent);
			return buildResponse(sentiment);
		}
		return buildResponse("nothing");
	}

}