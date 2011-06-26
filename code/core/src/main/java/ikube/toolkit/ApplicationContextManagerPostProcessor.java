package ikube.toolkit;

import ikube.IConstants;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class ApplicationContextManagerPostProcessor implements BeanFactoryPostProcessor {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			DataSource dataSource = ApplicationContextManager.getBean(IConstants.DATA_SOURCE_H2);
			dataSource.getConnection().createStatement().executeUpdate("create sequence system");
		} catch (Exception e) {
			logger.error("Exception creating the sequence on the H2 database : ", e);
		}
	}

}
