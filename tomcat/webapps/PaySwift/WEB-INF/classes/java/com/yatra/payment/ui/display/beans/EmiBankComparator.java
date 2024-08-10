package com.yatra.payment.ui.display.beans;

import java.util.Comparator;

public class EmiBankComparator implements Comparator<EMIBank> {

	public int compare(EMIBank o1, EMIBank o2) {

		if (o1 == null || o1.getDisplayText() == null)
			return 1;
		else if (o2 == null || o2.getDisplayText() == null)
			return -1;
		else
			return o1.getDisplayText().compareTo(o2.getDisplayText());
	}
}
