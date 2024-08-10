package com.yatra.payment.ui.display.beans;

import java.util.Comparator;

public class BankComparator implements Comparator<Bank> {

	public int compare(Bank o1, Bank o2) {

		if (o1 == null || o1.getDisplayText() == null)
			return 1;
		else if (o2 == null || o2.getDisplayText() == null)
			return -1;
		else
			return o1.getDisplayText().compareTo(o2.getDisplayText());
	}
}
