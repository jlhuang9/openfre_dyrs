package com.dyrs.hcq.utils;

import java.util.Arrays;

public class StringUtils {
    public static String makeNewString(String s1, String s2) {
        String[] strings = new String[] {s1, s2};
        Arrays.sort(strings);
        return String.join(":", strings);
    }


    public static String makeNewString(String s1, String s2, String area) {
        String result = "";
        if (s1.contains(area)) {
            String[] strings = new String[] {area, s2};
            Arrays.sort(strings);
            result = String.join(":", strings);
        } else if (s2.contains(area)) {
            String[] strings = new String[] {area, s1};
            Arrays.sort(strings);
            result = String.join(":", strings);
        }
        return result;
    }


}
