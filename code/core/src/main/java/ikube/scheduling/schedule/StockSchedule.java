package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.scheduling.Schedule;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static ikube.toolkit.FileUtilities.*;
import static ikube.toolkit.HttpClientUtilities.doGet;
import static ikube.toolkit.ThreadUtilities.sleep;
import static ikube.toolkit.XmlUtilities.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 26-09-1024
 */
public class StockSchedule extends Schedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockSchedule.class);
    @Value("${stock-api-uri}")
    // ?s=ACLZ&d=8&e=26&f=2014&g=d&a=2&b=27&c=2014&ignore=.csv
    private String stockApiUri = "http://ichart.yahoo.com/table.csv";
    @Value("${stock-sector-file-name}")
    private String stockSectorFileName = "all-stock-symbols-yahoo-finance.xml";
    @Value("${stock-sector-tag-name}")
    private String sectorTagName = "industry";
    @Value("${stock-sector-tag-attribute-name}")
    private String sectorTagAttributeName = "id";
    @Value("${stock-sector-tag-attribute-value}")
    private String sectorTagAttributeValue = "350";
    @Value("${stock-company-tag-name}")
    private String companyTagName = "company";
    @Value("${stock-company-tag-attribute-symbol-name}")
    private String companyTagAttributeSymbolName = "symbol";
    @Value("${stock-prices-from}")
    private String stockPricesFrom = "2012";
    @Value("${stock-prices-to}")
    private String stockPricesTo = "2014";

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        String[] parameterNames = {"s", "d", "e", "f", "g", "a", "b", "c", "ignore"};
        String[] parameterValues = new String[parameterNames.length];
        parameterValues[1] = "8";
        parameterValues[2] = "26";
        parameterValues[3] = stockPricesTo;
        parameterValues[4] = "d";
        parameterValues[5] = "2";
        parameterValues[6] = "27";
        parameterValues[7] = stockPricesFrom;
        parameterValues[8] = ".csv";

        File file = findFileRecursively(new File(IConstants.ANALYTICS_DIRECTORY), stockSectorFileName);
        try (InputStream inputStream = new FileInputStream(file)) {
            Document document = getDocument(inputStream, IConstants.ENCODING);
            Element sectorElement = getElement(document.getRootElement(), sectorTagName, sectorTagAttributeName, sectorTagAttributeValue);
            List<Element> companyElements = getElements(sectorElement, companyTagName);
            for (final Element companyElement : companyElements) {
                String companySymbol = null;
                try {
                    companySymbol = getAttributeValue(companyElement, companyTagAttributeSymbolName);
                    parameterValues[0] = companySymbol;
                    String result = doGet(stockApiUri, parameterNames, parameterValues, String.class);
                    LOGGER.error("Result : " + result);
                    File outputFile = getOrCreateFile(new File(IConstants.ANALYTICS_DIRECTORY, companySymbol + ".csv"));
                    setContents(outputFile, result.getBytes());
                    sleep(1000);
                } catch (Exception e) {
                    LOGGER.error("Exception accessing the stock : " + companySymbol, e);
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }
}