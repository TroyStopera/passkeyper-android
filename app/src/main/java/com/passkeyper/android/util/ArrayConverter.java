package com.passkeyper.android.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Contains methods for changing arrays to different types.
 */
public class ArrayConverter {

    private ArrayConverter() {
        //no need for instantiation
    }

    /**
     * Transforms a char[] into a byte[].
     *
     * @param chars the chars to transform.
     * @return the new byte[].
     */
    public static byte[] charsToBytes(char[] chars) {
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(CharBuffer.wrap(chars));

        byte[] bytes = Arrays.copyOfRange(
                byteBuffer.array(),
                byteBuffer.position(),
                byteBuffer.limit()
        );

        //clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    /**
     * Transforms a byte[] into a char[].
     *
     * @param bytes the bytes to transform.
     * @return the new char[].
     */
    public static char[] bytesToChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);

        char[] chars = new char[charBuffer.remaining()];
        charBuffer.get(chars);

        //clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);
        Arrays.fill(charBuffer.array(), '\0');

        return chars;
    }

}
