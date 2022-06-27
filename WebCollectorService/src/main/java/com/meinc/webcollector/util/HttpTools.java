package com.meinc.webcollector.util;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HttpTools {
    private static Log log = LogFactory.getLog(HttpTools.class);
    
    public static final Object PARAM_NAME = new Object();
    public static final Object PARAM_VALUE = new Object();

    private static void substituteNameAndValue(Object[] errorLogValues, String paramName, Object paramValue) {
        for (int i = 0; i < errorLogValues.length; i++)
            if (errorLogValues[i] == PARAM_NAME)
                errorLogValues[i] = paramName;
            else if (errorLogValues[i] == PARAM_VALUE)
                errorLogValues[i] = paramValue;
    }
    
    private static String getParamValue(Map<String,String> params, String paramName, Object[] errorLogValues) {
        String paramValue = params.get(paramName);
        paramValue = (paramValue == null) ? null : paramValue.trim();
        if (paramValue == null || paramValue.isEmpty()) {
            if (errorLogValues != null && errorLogValues.length > 0) {
                substituteNameAndValue(errorLogValues, paramName, paramValue);
                log.warn(String.format((String)errorLogValues[0], Arrays.copyOfRange(errorLogValues, 1, errorLogValues.length)));
            }
            return null;
        }
        return paramValue;
    }

    public static boolean parseIntParam(Map<String,String> params, String paramName, Mutable<Integer> result, Object...errorLogValues) {
        String paramValue = getParamValue(params, paramName, errorLogValues);
        if (paramValue == null)
            return false;
        try {
            result.set(Integer.parseInt(paramValue));
        } catch (Exception e) {
            if (errorLogValues != null && errorLogValues.length > 0) {
                substituteNameAndValue(errorLogValues, paramName, paramValue);
                log.warn(String.format((String)errorLogValues[0], Arrays.copyOfRange(errorLogValues, 1, errorLogValues.length)));
            }
            return false;
        }
        return true;
    }
    
    public static boolean parseStringParam(Map<String,String> params, String paramName, Mutable<String> result, Object...errorLogValues) {
        String paramValue = getParamValue(params, paramName, errorLogValues);
        if (paramValue == null)
            return false;
        result.set(paramValue);
        return true;
    }

    public static boolean parseDoubleParam(Map<String,String> params, String paramName, Mutable<Double> result, Object...errorLogValues) {
        String paramValue = getParamValue(params, paramName, errorLogValues);
        if (paramValue == null)
            return false;
        try {
            result.set(Double.parseDouble(paramValue));
        } catch (Exception e) {
            if (errorLogValues != null && errorLogValues.length > 0) {
                substituteNameAndValue(errorLogValues, paramName, paramValue);
                log.warn(String.format((String)errorLogValues[0], Arrays.copyOfRange(errorLogValues, 1, errorLogValues.length)));
            }
            return false;
        }
        return true;
    }
    
    public static class Mutable<T> {
        public T value;
        public T get() {
            return value;
        }
        public void set(T value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value.toString();
        }
    }
}
