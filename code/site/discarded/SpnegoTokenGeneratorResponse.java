package ikube.toolkit;

import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSContext;

public class SpnegoTokenGeneratorResponse {

	/**
	 * The gss context
	 */
	private GSSContext context;

	/**
	 * The generated token
	 */
	private byte[] token;

	public GSSContext getContext() {
		return context;
	}

	public void setContext(GSSContext context) {
		this.context = context;
	}

	public byte[] getToken() {
		return token;
	}
	
	public String getTokenAsAuthorizationHeaderValue(){
		if(token != null){
			byte[] encodedSpnegoToken = Base64.encodeBase64(token);
			return new String("Negotiate " + new String(encodedSpnegoToken));
		}else{
			return null;
		}
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

}
