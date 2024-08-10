package com.yatra.payment.ui.util;

public class EnvironmentVariableReader {

    /**
     * Get the value of a system-level environment variable.
     *
     * @param variableName The name of the environment variable to read.
     * @return The value of the environment variable, or null if it doesn't exist.
     */
    public static String getEnvironmentVariable(String variableName) {
        return System.getenv(variableName);
    }
}
