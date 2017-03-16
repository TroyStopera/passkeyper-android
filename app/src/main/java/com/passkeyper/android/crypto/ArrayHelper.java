package com.passkeyper.android.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Contains methods for changing arrays to different types.
 */
class ArrayHelper {

    /**
     * Transforms a char[] into a byte[].
     *
     * @param chars the chars to transform.
     * @return the new byte[].
     */
    static byte[] charsToBytes(char[] chars) {
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
    static char[] bytesToChars(byte[] bytes) {
        CharBuffer charBuffer = ByteBuffer.wrap(bytes).asCharBuffer();

        char[] chars = Arrays.copyOfRange(
                charBuffer.array(),
                charBuffer.position(),
                charBuffer.limit()
        );

        //clear sensitive data
        Arrays.fill(charBuffer.array(), '\0');

        return chars;
    }

}
