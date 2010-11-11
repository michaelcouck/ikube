package ikube.model;

import javax.persistence.Entity;

@Entity()
public class Ip extends Persistable {

	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
