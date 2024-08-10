package com.yatra.payment.ui.display.beans;

import java.util.Comparator;

public class PreferredBankComparator implements Comparator<Bank> {

    @Override
    public int compare(Bank o1, Bank o2) {
        if (o1 == null || o1.getPriority() == null)
            return 1;
        else if (o2 == null || o2.getPriority() == null)
            return -1;
        else
            return o1.getPriority().compareTo(o2.getPriority());
    }
}
