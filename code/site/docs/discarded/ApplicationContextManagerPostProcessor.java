package ikube.toolkit;

import ikube.IConstants;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * This is a class that can execute arbitrary logic after the context is initialized.
 * 
 * @author Michael Couck
 * @since 21.06.11
 * @version 01.00
 */
public class ApplicationContextManagerPostProcessor implements BeanFactoryPostProcessor {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Connection connection = null;
		Statement statement = null;
		try {
			DataSource dataSource = ApplicationContextManager.getBean(IConstants.DATA_SOURCE_H2);
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			statement.executeUpdate("create sequence system");
		} catch (Exception e) {
			logger.error("Exception creating the sequence on the database : ", e);
		} finally {
			DatabaseUtilities.close(statement);
			DatabaseUtilities.close(connection);
		}
	}

}