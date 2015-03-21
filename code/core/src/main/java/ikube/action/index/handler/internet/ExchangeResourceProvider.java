package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.internet.exchange.ExchangeClient;
import ikube.model.IndexableMessage;
import ikube.model.IndexableExchange;

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
    private boolean terminated;

    /* provider */
    public ExchangeResourceProvider(final IndexableExchange indexableExchange) {
        this.indexableExchange = indexableExchange;

        messages = getExchangeClientInstance().getUsersMessages(
                indexableExchange.getIndexFromDate(),
                new Date(),
                indexableExchange.getResumeIndexFrom(),
                indexableExchange.getResumeIndexFromMessage());
    }

    /**
     * TODO: store Exchange connection information in a configuration file
     * EWS_URL  = "https://outlook.office365.com/ews/exchange.asmx";
     * EWS_USERNAME = "administrator@iKube.onmicrosoft.com";
     * EWS_PASSWORD = "iKube101";
     */
    private ExchangeClient getExchangeClientInstance() {
        if (exchange == null) {
            try {
                exchange = new ExchangeClient(
                        indexableExchange.getExchangeUrl(), null,
                        indexableExchange.getExchangeUserid(),
                        indexableExchange.getExchangePassword());
                // optional: exchange.setRequestVersion(RequestServerVersion.EXCHANGE_2013);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Unable to establish a connection with Exchange server URL [" +
                                indexableExchange.getExchangeUrl() + "]. Root cause is " + e.getMessage(), e);
            }
        }
        return exchange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
    }

    /**
     * {@inheritDoc}
     * consumer
     */
    @Override
    public IndexableMessage getResource() {
        // TODO: Return a crawled resource to the caller
        if (isTerminated()) {
            return null;
        }
        return messages.next();
    }

    /**
     * feedback from consumer threads - indexablehandler class
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<IndexableMessage> resources) {
        // TODO: Add the resource crawled to the collection available in this object
    }

}