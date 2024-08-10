package com.yatra.payment.ui.display.beans;

import java.util.Comparator;

public class PaymentOptionComparator implements Comparator<PaymentOption> {

	public int compare(PaymentOption o1, PaymentOption o2) {
		if (o1 == null || o1.getPriority() == null || o1.getPriority() == -1)
			return 1;
		else if (o2 == null || o2.getPriority() == null || o2.getPriority() == -1)
			return -1;
		else
			return o1.getPriority().compareTo(o2.getPriority());
	}
}
