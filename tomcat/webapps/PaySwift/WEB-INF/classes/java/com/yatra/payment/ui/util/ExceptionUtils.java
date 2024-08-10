package com.yatra.payment.ui.util;

public class ExceptionUtils {
	
	public static String toString(Exception e){
		StringBuilder sb = new StringBuilder();
		sb.append("[exception ").append(e.getClass().getName()).append(":").append(e.getMessage());
		if(e.getCause() != null){
			sb.append(", cause ").append(e.getCause().getClass()).append(":").append(e.getCause().getMessage());
		}
		sb.append("]");
		return sb.toString();
	}
	
}
