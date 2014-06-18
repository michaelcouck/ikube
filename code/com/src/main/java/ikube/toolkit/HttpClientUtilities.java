package ikube.toolkit;

import ikube.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AutoRetryHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * TODO: Migrate to {@link com.sun.jersey.api.client.Client}.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 17-06-2014
 */
public class HttpClientUtilities {

	private static final HttpClient HTTP_CLIENT = new AutoRetryHttpClient();

	public static <T> T doGet(final String url, final Class<T> type) {
		HttpGet httpGet = new HttpGet(url);
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) {
				try {
					return FileUtilities.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		try {
			String response = HTTP_CLIENT.execute(httpGet, responseHandler);
			return Constants.GSON.fromJson(response, type);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T doGet(final String url, final Class<T> type, final String[] names, final String[] values) {
		HttpParams httpParams = new BasicHttpParams();
		for (int i = 0; i < names.length; i++) {
			httpParams.setParameter(names[i], values[i]);
		}

		HttpGet httpGet = new HttpGet(url);
		httpGet.setParams(httpParams);
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) {
				try {
					return FileUtilities.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		try {
			String response = HTTP_CLIENT.execute(httpGet, responseHandler);
			return Constants.GSON.fromJson(response, type);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T doPost(final String url, final Object entity, final Class<T> type) {
		HttpEntity httpEntity = new StringEntity(Constants.GSON.toJson(entity), APPLICATION_JSON);
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		httpPost.setEntity(httpEntity);
		try {
			String response = HTTP_CLIENT.execute(httpPost, getResponseHandler());
			return Constants.GSON.fromJson(response, type);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T, U> U doPost(final String url, final Object entity, final Class<T> inputType, final Class<U> outputType) {
		HttpEntity httpEntity = new StringEntity(Constants.GSON.toJson(entity), APPLICATION_JSON);
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
		httpPost.setEntity(httpEntity);
		try {
			String response = HTTP_CLIENT.execute(httpPost, getResponseHandler());
			return Constants.GSON.fromJson(response, outputType);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ResponseHandler<String> getResponseHandler() {
		return new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) {
				try {
					return FileUtilities.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

}
