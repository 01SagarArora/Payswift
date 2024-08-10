package com.yatra.payment.ui.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LoggingUtil {
    private static Logger logger = Logger.getLogger(LoggingUtil.class);

    public static final String REGEX_PRESERVE_NONE = ".";
    public static final String REGEX_PRESERVE_LAST_FOUR = ".(?=.{4})";

    public static final String REGEX_PRESERVE_FIRST_SIX = "(?<=......).";




    public static String getMaskedJsonForLogging(Object paramJsonObject , Map<String,String> fieldsToMaskMap){
        JSONObject maskedJson = null  ;
        try {
            maskedJson = new JSONObject(paramJsonObject.toString());
            iterateJsonObject(maskedJson ,fieldsToMaskMap);
            return  maskedJson.toString();
        }
        catch (Exception ex){
            logger.error("error occurred while masking data ",ex);

        }
        return null;

    }

    public   static void   iterateJsonObject(JSONObject paramJsonObject  , Map<String,String> fieldsToMaskMap) throws Exception {

        if (paramJsonObject != null) {

            Iterator<String> keys = paramJsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = paramJsonObject.get(key);
                if (value instanceof JSONObject) {
                    iterateJsonObject((JSONObject) value, fieldsToMaskMap );
                } else if (value instanceof JSONArray) {
                    iterateJsonArray((JSONArray) value ,fieldsToMaskMap);
                } else {
                    getMaskedNodeValue(paramJsonObject, key, value.toString(), fieldsToMaskMap);
                }
            }
        }
   }

    public  static   void iterateJsonArray(JSONArray paramJsonArray,   Map<String,String> fieldsToMaskMap) throws Exception {
        if (paramJsonArray != null) {
            for (int i = 0; i < paramJsonArray.length(); i++) {
                Object value = paramJsonArray.get(i);
                if (value instanceof JSONObject) {
                    iterateJsonObject((JSONObject) value ,fieldsToMaskMap);
                } else if (value instanceof JSONArray) {
                    iterateJsonArray((JSONArray) value ,fieldsToMaskMap);
                }
            }
        }
    }

    public  static  void getMaskedNodeValue(JSONObject paramJsonObject , String node, String value , Map<String,String> fieldsToMaskMap) throws  Exception {

        if (!fieldsToMaskMap.isEmpty() &&
                fieldsToMaskMap.containsKey(node) &&
                isValidRegex(fieldsToMaskMap.get(node))) {
            String regex = fieldsToMaskMap.get(node);
            paramJsonObject.put(node, value.replaceAll(regex, "x"));
        }
    }
   public static boolean isValidRegex(String regex){
       try {
           Pattern.compile(regex);
       } catch (PatternSyntaxException exception) {
           logger.error("Invalid pattern : "+exception);
           return  false;
       }
    return  true;
    }

    }

