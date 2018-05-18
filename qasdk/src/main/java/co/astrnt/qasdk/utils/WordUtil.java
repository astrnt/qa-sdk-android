package co.astrnt.qasdk.utils;

public class WordUtil {

    public static String createAlphabet(int i) {
        return i < 0 ? "" : createAlphabet((i / 26) - 1) + (char) (65 + i % 26);
    }
}
