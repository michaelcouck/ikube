package ikube.experimental.publisher;

public class Push {

    private String api_key;

    private Data data;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(final String api_key) {
        this.api_key = api_key;
    }

    public Data getData() {
        return data;
    }

    public void setData(final Data data) {
        this.data = data;
    }

}
