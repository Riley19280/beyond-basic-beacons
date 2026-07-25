package com.rileystech.beyondbasicbeacons;

public class Utility {
    public static String convertNumberToRoman(int number) {
        final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < VALUES.length && number > 0; i++) {
            while (VALUES[i] <= number) {
                number -= VALUES[i];
                result.append(SYMBOLS[i]);
            }
        }
        return result.toString();
    }
}
