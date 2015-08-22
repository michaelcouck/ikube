package ikube.experimental.publisher;

public class Event {

    private String accessKey;
    private String streamName;
    private Object point;

    public Object getPoint() {
        return point;
    }

    public void setPoint(final Object point) {
        this.point = point;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(final String streamName) {
        this.streamName = streamName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

}
