package com.yatra.payment.ui.cache.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yatra.payment.cache.service.UICacheManager;
import com.yatra.payment.ui.beans.convertor.BeanConvertor;
import com.yatra.payment.ui.dao.PaymentUIGenericDAO;
import com.yatra.payment.ui.dao.bean.UICardTypes;
import com.yatra.payment.ui.display.beans.CardType;

@Service("cardTypeCacheBuilder")
public class CardTypeCacheBuilder implements CacheBuilder {

	private static Logger logger = Logger.getLogger(CardTypeCacheBuilder.class);

	@Autowired	private PaymentUIGenericDAO<UICardTypes> cardTypeDao;
	@Autowired	private UICacheManager<CardType> cardTypeCache;
	@Autowired	private BeanConvertor<UICardTypes, CardType> cardTypeConvertor;

	public void build() {
		try {
			List<UICardTypes> uiCardTypes = cardTypeDao.getAll();
			if (uiCardTypes != null) {
				for (UICardTypes uiCardType : uiCardTypes) {
					CardType cardType = cardTypeConvertor.convert(uiCardType);
					cardTypeCache.set(cardType.getCode(), cardType);
				}
			}
			logger.info("UI card type Cache build successfully. Total " + uiCardTypes.size() + " message mapping ");
		} catch (Exception ex) {
			logger.error("SEVERE!! Error occurred while creating CardTypeCache. Payment UI will not be rendered.", ex);
		}
	}
}