package com.yatra.payment.ui.tag;

import com.yatra.platform.ui.BaseTagHandler;

/**
 * This class is used to display yatra custom tag for images, css and
 * javascript.
 * 
 * @author deolm
 * 
 */

public class LanguageTagHandler extends BaseTagHandler {

	private String language;
	private String content;
	private boolean identifier;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public boolean getIdentifier() {
		return identifier;
	}

	public void setIdentifier(boolean identifier) {
		this.identifier = identifier;
	}

	@Override
	public String printBody() {

		return content;
	}
}
