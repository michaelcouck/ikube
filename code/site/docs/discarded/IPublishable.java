package ikube.service;

public interface IPublishable {

	int getPort();

	String getPath();

	void setPort(final int port);

	void setPath(final String path);

}
