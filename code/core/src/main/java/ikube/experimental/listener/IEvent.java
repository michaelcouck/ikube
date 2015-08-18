package ikube.experimental.listener;


import ikube.experimental.Context;

public interface IEvent<Source, Data> {

    Context getContext();

    Data getData();

    Source getSource();

}
