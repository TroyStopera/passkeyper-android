package com.passkeyper.android.util;

import android.widget.EditText;

/**
 * Utility class that contains helper methods for EditTexts..
 */
public class EditTextUtils {

    private EditTextUtils() {
        //no need for instantiation
    }

    /**
     * Gets the EditText's text in the form of a char[].
     *
     * @param editText the EditText to get the text from.
     * @return the text from the EditText.
     */
    public static char[] getText(EditText editText) {
        int len = editText.length();
        char[] text = new char[len];
        editText.getText().getChars(0, len, text, 0);
        return text;
    }

}
