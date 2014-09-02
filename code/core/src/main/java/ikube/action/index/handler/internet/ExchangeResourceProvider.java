package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableExchange;
import ikube.action.index.handler.internet.exchange.ExchangeClient;
import ikube.action.index.handler.internet.exchange.IndexableMessage;

import java.util.Date;
import java.util.List;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class ExchangeResourceProvider implements IResourceProvider<IndexableMessage> {

    private final IndexableExchange indexableExchange;

    private ExchangeClient exchange;
    private ExchangeClient.UsersMessages messages;

    /**
     * TODO: store Exchange connection information in a configuration file
     *   EWS_URL  = "https://outlook.office365.com/ews/exchange.asmx";
     *   EWS_USERNAME = "administrator@iKube.onmicrosoft.com";
     *   EWS_PASSWORD = "iKube101";
     */
    private ExchangeClient getExchangeClientInstance(){
        if(exchange == null){
            try {
                exchange = new ExchangeClient(
                        indexableExchange.getUrl(), null,
                        indexableExchange.getUserid(),
                        indexableExchange.getPassword());
                // optional: exchange.setRequestVersion(RequestServerVersion.EXCHANGE_2013);
            }catch(Exception e){
                throw new IllegalArgumentException(
                    "Unable to establish a connection with Exchange server URL [" +
                    indexableExchange.getUrl() +"]. Root cause is " + e.getMessage(), e);
            }
        }
        return exchange;
    }

    /* provider */
    public ExchangeResourceProvider(final IndexableExchange indexableExchange) {
        this.indexableExchange  = indexableExchange;

        messages = getExchangeClientInstance().getUsersMessages(
                indexableExchange.getIndexFromDate(),
                new Date(),
                indexableExchange.getResumeIndexFrom(),
                indexableExchange.getResumeIndexFromMessage());
    }

    /**
     * {@inheritDoc}
     * consumer
     */
    @Override
    public IndexableMessage getResource() {
        // TODO: Return a crawled resource to the caller
        return messages.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<IndexableMessage> resources) {
        // TODO: Add the resource crawled to the collection available in this object
    }

}