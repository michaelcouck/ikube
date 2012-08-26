package ikube.ant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class RemoteExecuter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteExecuter.class);

	public static void main(String[] args) {
		try {
			JSch jsch = new JSch();
			Session session=jsch.getSession("michael", "192.168.1.4", 22);
			UserInfo ui=new UserInfo() {
				
				@Override
				public void showMessage(String message) {
					LOGGER.info(message);
				}
				
				@Override
				public boolean promptYesNo(String message) {
					return false;
				}
				
				@Override
				public boolean promptPassword(String message) {
					return false;
				}
				
				@Override
				public boolean promptPassphrase(String message) {
					return false;
				}
				
				@Override
				public String getPassword() {
					return "caherline";
				}
				
				@Override
				public String getPassphrase() {
					return null;
				}
			};
		      session.setUserInfo(ui);
		      session.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
