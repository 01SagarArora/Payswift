/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yatra.payment.offline.util;

/**
 *
 * @author YATRAONLINE\rohit.lohia
 */
public class VendorCodeEnum {
    
    public static enum VendorCodes {
        VISA("VISA","VI"),
        MASTER("MASTER","CA"),
        AMEX("AMEX", "AX"),
        DINERS("DINERS", "DC"),
        DINNERS("DINNERS", "DC"),
        JCB("JCB","JC"),
        AIRPLUS("AIRPLUS","TP");
        
        private String brand;
        private String code;
        VendorCodes(String brand, String code) {
            this.brand = brand;
            this.code = code;
        }
        
        public String getBrand() {
            return this.brand;
        }
        
        public String getCode() {
            return this.code;
        }
        
        public static String getCodeForBrand(String brand) {
            for (VendorCodes v: VendorCodes.values()) {
                if (v.getBrand().equalsIgnoreCase(brand)) {
                    return v.getCode();
                }
            }
            return null;
        }
    }
}
