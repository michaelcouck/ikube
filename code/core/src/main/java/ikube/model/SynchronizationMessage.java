package ikube.model;

import javax.persistence.Entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 18.12.10
 * @version 01.00
 */
@Entity()
public class SynchronizationMessage extends Persistable {

	private String ip;
	private Integer port;
	private String filePath;
	private Long fileLength;

	public String getIp() {
		return ip;
	}

	public void setIp(final String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	public Long getFileLength() {
		return fileLength;
	}

	public void setFileLength(final Long fileLength) {
		this.fileLength = fileLength;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
