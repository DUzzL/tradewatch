package dev.duzzl.tradewatch.util;
public final class RomanNumerals { private static final String[] VALUES = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"}; private RomanNumerals() { } public static String of(int value) { return value > 0 && value < VALUES.length ? VALUES[value] : Integer.toString(value); } }
