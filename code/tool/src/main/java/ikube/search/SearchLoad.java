package ikube.search;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import ikube.IConstants;
import ikube.Load;
import ikube.model.Search;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This class will just execute a search on an index several times, and the results and
 * performance can be checked visually to verify that there are no bottlenecks in the search
 * logic.
 * <p/>
 * <pre>
 *     java -jar ikube-tool-4.4.1-SNAPSHOT.jar ikube.search.SearchLoad -u localhost -p 9090 -s passwords -d desktop -f contents
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-04-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class SearchLoad extends Load {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchLoad.class);

	@Option(name = "-t")
	private int threads = 5;
	@Option(name = "-i")
	private int iterations = 1000000;
	@Option(name = "-u")
	private String url = "ikube.be";
	@Option(name = "-p")
	private int port = 80;
	@Option(name = "-s")
	private String searchString = "cape town";
	@Option(name = "-d")
	private String indexName = IConstants.GEOSPATIAL;
	@Option(name = "-f")
	private String fieldName = IConstants.NAME;

	public static void main(final String[] args) throws Exception {
		new SearchLoad().doMain(args);
	}

	protected void doMain(final String[] args) throws Exception {
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(140);
		parser.parseArgument(args);

		ThreadUtilities.initialize();

		Search search = new Search();
		search.setFirstResult(0);
		search.setMaxResults(10);
		search.setDistributed(Boolean.FALSE);
		search.setSearchStrings(Arrays.asList(searchString));

		search.setFragment(Boolean.TRUE);
		search.setIndexName(indexName);
		search.setOccurrenceFields(Arrays.asList("must"));
		search.setSearchFields(Arrays.asList(fieldName));
		search.setTypeFields(Arrays.asList("string"));

		final String url = getUrl(this.url, this.port, "/ikube/service/search/json");
		final String content = IConstants.GSON.toJson(search);

		int count = 0;
		do {
			final Client client = Client.create();
			client.addFilter(new HTTPBasicAuthFilter("user", "user"));
			class Timed implements Timer.Timed {
				@Override
				public void execute() {
					int searches = iterations / threads;
					do {
						WebResource webResource = client.resource(url);
						ClientResponse clientResponse = webResource.accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(ClientResponse.class, content);
						if (searches % 1000 == 0) {
							String response = clientResponse.getEntity(String.class);
							Search search = IConstants.GSON.fromJson(response, Search.class);
							LOGGER.info("Searches : " + searches + ", " + search.getCount() + ", " + search.getTotalResults());
							LOGGER.info("Search : " + search);
						}
					} while (searches-- >= 0);
				}
			}

			final Timer.Timed timed = new Timed();
			class Runner implements Runnable {
				@Override
				public void run() {
					double duration = Timer.execute(timed);
					LOGGER.info("Duration for searching : " + iterations + ", is : " + duration + ", per second is : " + iterations / (duration / 1000));
				}
			}

			Runnable runnable = new Runner();
			ThreadUtilities.submit(runnable.toString(), runnable);
		} while (count++ < threads);
	}

}