package ikube.util;

import org.springframework.web.context.support.WebApplicationObjectSupport;

public class ApplicationObjectSupport extends WebApplicationObjectSupport {
	
	public String getMessage(String key) {
		return getMessageSourceAccessor().getMessage(key);
	}
	
	public String getMessage(String key, String... args) {
		return getMessageSourceAccessor().getMessage(key, args);
	}

}
