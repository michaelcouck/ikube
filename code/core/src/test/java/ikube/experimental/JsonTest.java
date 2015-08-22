package ikube.experimental;

import com.google.gson.Gson;
import ikube.experimental.publisher.Data;
import ikube.experimental.publisher.Event;
import ikube.experimental.publisher.Item;
import ikube.experimental.publisher.Push;
import org.junit.Test;

import java.util.Arrays;

public class JsonTest extends AbstractTest {

    @Test
    public void toJson() {
        Push push = new Push();
        Data data = new Data();
        Item item = new Item();

        push.setData(data);
        push.setApi_key("api_key");

        data.setItem(Arrays.asList(item));

        Event eventOne = getEvent("Data", "125");
        Event eventTwo = getEvent("Data", "562");
        Event eventThree = getEvent("Data", "225");

        item.setText("text");
        item.setValue("123.132");

        // item.setEvents(Arrays.asList(eventOne, eventTwo, eventThree));

        Gson gson = new Gson();

        System.out.println(gson.toJson(push));
    }

    private Event getEvent(final String text, final String value) {
        Event event = new Event();
        // event.setText(text);
        // event.setValue(value);
        return event;
    }

}
