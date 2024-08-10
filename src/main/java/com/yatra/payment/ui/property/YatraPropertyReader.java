package com.yatra.payment.ui.property;

import java.util.Properties;

import org.apache.log4j.Logger;

public class YatraPropertyReader {
	
	private Logger logger = Logger.getLogger(YatraPropertyReader.class);
	private Properties properties;

	public void setHolder(YatraPropertiesConfigurer holder) {
		properties = holder.getMergedProperties();
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Boolean getPropertyAsBoolean(String key) {
		return Boolean.valueOf(properties.getProperty(key));
	}

	public Integer getPropertyAsInt(String key) {
		try {
			return Integer.parseInt(properties.getProperty(key).trim());
		} catch (NumberFormatException nfExp) {
			logger.error("Exception occurred while fetching property as Int : " + nfExp);
			return null;
		}
	}
}
