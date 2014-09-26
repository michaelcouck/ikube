package ikube.toolkit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.util.Arrays;

/**
 * Utilities for strings that are not available in the general string classes from Apache or Spring.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-01-2012
 */
public final class StringUtilities {

    public static final char SPACE = ' ';
    public static final char[] EXCLUSIONS = {'.', ',', ';', ':', '?', '!', '\'', '-'};

    static {
        Arrays.sort(EXCLUSIONS);
    }

    /**
     * This method will check to see if a string can be parsed into a number of sorts,
     * like a double for example. If there is a separator like a '.' or a ','
     * then this will still qualify as a number, but more than one dot foe example then dis-qualifies the string as a
     * number.
     * <p/>
     * <pre>
     * 1) 123 - true
     * 2) 123.456 - true
     * 3) 123.456.789 - false
     * 4) 123.456,789 - false
     * 5) . - false
     * 6) 1. - false
     * 7) .1 - false
     * </pre>
     * <p/>
     * This method is faster that the string utilities from Apache, as a matter of interest.
     *
     * @param string the string to check whether it qualifies to be parsed into a number, could be a double
     * @return whether the string has only numbers and potentially one separator a comma or a dot, but no spaces
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isNumeric(final String string) {
        if (string == null) {
            return Boolean.FALSE;
        }
        final char[] chars = string.toCharArray();
        boolean before = Boolean.FALSE;
        boolean dot = Boolean.FALSE;
        boolean after = Boolean.FALSE;
        for (final char c : chars) {
            if (Character.isDigit(c)) {
                if (!dot) {
                    before = Boolean.TRUE;
                } else {
                    after = Boolean.TRUE;
                }
            } else if ('.' == c || ',' == c) {
                if (dot) {
                    // If we find more than one dot or number separator
                    // then this is not a number per se but some kind of string
                    // identifier like 123.456.789, which is not numeric
                    return Boolean.FALSE;
                }
                dot = Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        // There must be at least one number in the string, and if there is a dot
        // then there must be a number before the dot and one after the dot/comma
        return (before && !dot) || (before && dot && after);
    }

    /**
     * This method will check that the string is a valid date of the format 'yyyy-MM-dd'. It is lenient
     * so strings like '71-5-1' will also be valid, it will be assumed that the year is a shortened version,
     * and month and day.
     *
     * @param dateString the string to check for valie date foemat
     * @return whether the string can be parsed into a date
     */
    public static boolean isDate(final String dateString) {
        try {
            DateUtils.parseDate(dateString, new String[]{"yyyy-MM-dd"});
            return Boolean.TRUE;
        } catch (final ParseException e) {
            return Boolean.FALSE;
        }
    }

    /**
     * This method strips all the characters out of the target string that are specified in the parameter list. For example
     * the string 'Hello! World!' stripped of '!' will become, 'Hello World', having had the exclamation marks removed.
     *
     * @param string the string to strip of characters
     * @param strip  the characters to strip from teh string
     * @return the stripped string, sans the characters for strip
     */
    public static String strip(final String string, final String strip) {
        char[] chars = string.toCharArray();
        char[] strippedChars = new char[chars.length];
        char[] stripChars = strip.toCharArray();
        int j = 0;
        for (final char c : chars) {
            boolean equals = Boolean.FALSE;
            for (char stripChar : stripChars) {
                if (c == stripChar) {
                    equals = Boolean.TRUE;
                    break;
                }
            }
            if (!equals) {
                strippedChars[j++] = c;
            } else {
                if (j == 0) {
                    strippedChars[j++] = SPACE;
                } else {
                    if (strippedChars[j - 1] != SPACE) {
                        strippedChars[j++] = SPACE;
                    }
                }
            }
        }
        return new String(strippedChars, 0, j);
    }

    /**
     * Remove single characters and numbers and anything that isn't human
     * and strips the whitespace to one character if there are more than one. Will
     * also remove all punctuation. Note that this method will leave the Western language
     * basic punctuation marks.
     *
     * @param content the string to strip non-human readable characters from
     * @return the cleaned string, all alpha numeric characters
     */
    public static String stripToAlphaNumeric(final String content) {
        return stripToAlphaNumeric(content, EXCLUSIONS);
    }

    public static String stripToAlphaNumeric(final String content, final char... exclusions) {
        if (!StringUtils.isEmpty(content)) {
            StringBuilder builder = new StringBuilder();
            char previous = SPACE;
            char[] characters = content.toCharArray();
            for (final char c : characters) {
                boolean append = Boolean.FALSE;
                if (Character.isWhitespace(c)) {
                    if (previous != SPACE) {
                        append = Boolean.TRUE;
                    }
                } else if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                    append = Boolean.TRUE;
                } else {
                    if (Arrays.binarySearch(exclusions, c) >= 0) {
                        append = Boolean.TRUE;
                    } else if (previous != SPACE) {
                        previous = SPACE;
                        builder.append(SPACE);
                    }
                }
                if (append) {
                    previous = c;
                    builder.append(c);
                }
            }
            return StringUtils.stripToEmpty(builder.toString());
        }
        return content;
    }

}