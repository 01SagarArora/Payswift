package com.yatra.payment.core.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PaymentUtil 
{
	public static String getLocalIPAdress() {
		String ipAddrStr = "";

		try {
			InetAddress addr = InetAddress.getLocalHost();

			// Get IP Address
			byte[] ipAddr = addr.getAddress();

			for (int i = 0; i < ipAddr.length; i++) {
				if (i > 0) {
					ipAddrStr += ".";
				}
				ipAddrStr += ipAddr[i] & 0xFF;
			}

		} catch (UnknownHostException e) {
			ipAddrStr = "";
		}

		return ipAddrStr;
	}


}
