package com.example.tickerexp;

public final class Utils {

    private Utils(){
        throw new AssertionError("Cannot instantiate utility class");
    }
    public static boolean isStringInArray(String[] array, String target) {
        for (String element : array) {
            if (element.equals(target)) {
                return true;
            }
        }
        return false;
    }
}


