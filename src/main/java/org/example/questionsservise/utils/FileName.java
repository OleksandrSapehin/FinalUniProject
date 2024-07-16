package org.example.questionsservise.utils;

import org.apache.commons.text.StringEscapeUtils;

public class FileName {

    public static String sanitizeFileName(String input) {
        String decodedTitle = StringEscapeUtils.unescapeHtml4(input);

        return decodedTitle.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
