package ikube.toolkit;

import ikube.Base;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.Ignore;
import org.junit.Test;

public class AdHocTest extends Base {

	static {
		System.setProperty("java.security.auth.login.config", "src/test/java/jaas.conf");
		System.setProperty("java.security.krb5.realm", "POST.BPGNET.NET");
		System.setProperty("java.security.krb5.kdc", "post.bpgnet.net");
	}

	@Test
	public void myClient() throws Exception {
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod("http://sdl-st2.netpost/sdl-ws/UserService?wsdl");
		postMethod.setRequestHeader("Authorization", "");
		int response = httpClient.executeMethod(postMethod);
		String responseBody = postMethod.getResponseBodyAsString();
		logger.info(response);
		logger.info(responseBody);
	}

	@Test
	public void guiClient() throws Exception {
		HttpClient httpclient = new HttpClient();
		String body = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' "
				+ "<http://schemas.xmlsoap.org/soap/envelope/%27>  xmlns:ws='http://ws.sdl.post.be/' <http://ws.sdl.post.be/%27> ><soapenv:Header/><soapenv:Body><ws:getUsers>"
				+ "<!--Optional:--><arg0>0</arg0><!--Optional:--><arg1>0</arg1></ws:getUsers></soapenv:Body></soapenv:Envelope>";
		String bodyLength = new Integer(body.length()).toString();
		PostMethod httpPost = new PostMethod("http://10.192.52.248:8080/sdl-ws/UserService");
		httpPost.setRequestHeader("SOAPAction", "");
		httpPost.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
		httpPost.setRequestHeader("Content-Length", bodyLength);
		httpPost.setRequestHeader("Authorization", getToken());
		StringRequestEntity entity = new StringRequestEntity(body, "text/xml", "UTF-8");
		httpPost.setRequestEntity(entity);
		int response = httpclient.executeMethod(httpPost);
		System.out.println(response);
		String responseBody = FileUtilities.getContents(httpPost.getResponseBodyAsStream(), Integer.MAX_VALUE).toString();
		System.out.println(responseBody);
	}

	private String getToken() {
		SpnegoTokenGenerator generator = new SpnegoTokenGenerator();
		generator.setJaasConfigurationName("KerberosClient");
		SpnegoTokenGeneratorResponse token = generator.generateToken("HTTP/sdl-dv2.netpost@POST.BPGNET.NET");
		return token.getTokenAsAuthorizationHeaderValue();
	}

}