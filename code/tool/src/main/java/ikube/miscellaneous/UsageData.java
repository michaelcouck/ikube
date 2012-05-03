//package ikube.miscellaneous;
//
//import ikube.toolkit.FileUtilities;
//
//import java.io.File;
//import java.io.IOException;
//
//import org.codehaus.jackson.JsonFactory;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.JsonParser;
//import org.codehaus.jackson.JsonToken;
//
//public class UsageData {
//
//	public UsageData(final String filename, final String pluginName) throws JsonParseException, IOException {
//		JsonFactory jsonFactory = new JsonFactory();
//		JsonParser jp = jsonFactory.createJsonParser(new File(filename));
//
//		int installCount = 0;
//		int pluginCount = 0;
//
//		String name;
//		JsonToken token;
//		boolean inPluginArray = false;
//		boolean pluginSeen = false;
//		while (jp.nextToken() != null) {
//			token = jp.getCurrentToken();
//
//			switch (token) {
//			case VALUE_STRING:
//				break;
//			case START_ARRAY:
//				if (jp.getCurrentName().equals("plugins")) {
//					inPluginArray = true;
//				}
//				break;
//			case END_ARRAY:
//				inPluginArray = false;
//				break;
//			default:
//				// Skip all other token types
//				continue;
//			}
//
//			name = jp.getCurrentName();
//			if (token == JsonToken.START_ARRAY && name.length() == 64) {
//				// Found list of reports for a new installation
//				installCount++;
//				pluginSeen = false;
//			}
//
//			if (!pluginSeen && inPluginArray && token == JsonToken.VALUE_STRING && name.equals("name")) {
//				if (jp.getText().equals(pluginName)) {
//					pluginCount++;
//					pluginSeen = true;
//				}
//			}
//		}
//		jp.close();
//
//		System.out.println("Total install count: " + installCount);
//		System.out.println(pluginName + " count: " + pluginCount);
//	}
//
//	public static void main(String[] args) throws JsonParseException, IOException {
//		File[] files = FileUtilities.getFile("C:/Tmp/output", Boolean.TRUE).listFiles();
//		for (File file : files) {
//			new UsageData(file.getAbsolutePath(), "serenity");
//		}
//	}
//
// }
