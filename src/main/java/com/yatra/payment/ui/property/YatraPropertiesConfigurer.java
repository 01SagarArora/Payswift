package com.yatra.payment.ui.property;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class YatraPropertiesConfigurer extends PropertyPlaceholderConfigurer {

	private Logger logger = Logger.getLogger(YatraPropertiesConfigurer.class);
	private Properties mergedProperties;

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			mergedProperties = mergeProperties();
			convertProperties(mergedProperties);
			processProperties(beanFactory, mergedProperties);
			
		} catch (IOException ex) {
			logger.error("Could not load properties", ex);
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}

	public Properties getMergedProperties() {
		return mergedProperties;
	}
}
