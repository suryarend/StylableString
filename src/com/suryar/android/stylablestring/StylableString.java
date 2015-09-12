package com.suryar.android.stylablestring;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.util.SparseArray;

/**
 * This class enables styling strings found in strings.xml
 * Usage:
 * 1. Using spanned strings-
 * StylableString.format(context,
 * R.string.home_tile_sleep_value_hours_minutes_span,
 * StylableString.DEFAULT_STYLE_RES_ID, textLineHours, textLineMinutes);
 * 
 * 2. With styles for string placeholder -
 * StylableString.format(this, R.string.formatted_value, R.array.MetricFormat, "Hello", "World");
 * MetricFormat is defined in stringFormats.xml as
 * <string-array name="MerticFormat">
 * <item>1|MetricValue</item>
 * <item>2|MetricUnit</item>
 * </string-array>
 * Each item in the style array correspond to placeholder item in strings.xml
 */

public class StylableString {

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern fsPattern = Pattern.compile(formatSpecifier);

    public static final int DEFAULT_STYLE_RES_ID = -1;

    public static SpannableString format(Context context, int stringResId,
            int styleArrayResId, Object... argValues) {

    	if(context == null) {
    			throw new IllegalArgumentException("context is null");
    	}

        if (argValues == null || argValues.length < 1) {
            throw new IllegalArgumentException("argValues");
        }

        String resourceString = context.getString(stringResId);

        SparseArray<List<CharacterStyle>> styleArray = new SparseArray<List<CharacterStyle>>();

        if (styleArrayResId != DEFAULT_STYLE_RES_ID) {
            styleArray = getStringStyles(context, styleArrayResId);
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(resourceString);

        int i = 0;
        int argumentIndex = 0;

        while (i < sb.length()) {
            Matcher m = fsPattern.matcher(sb);

            if (!m.find(i)) {
                break;
            }

            i = m.start();
            int end = m.end();

            // max 6 groups from format specifier (above)
            String[] groups = new String[6];
            for (int j = 0; j < m.groupCount(); j++) {
                groups[j] = m.group(j + 1);
            }

            // if index is not specified in the format then argument index
            // string will be null/empty
            String argumentIndexString = groups[0];

            if (!TextUtils.isEmpty(argumentIndexString)) {
                argumentIndex = Integer.parseInt(argumentIndexString.substring(
                        0, argumentIndexString.length() - 1));
            } else {
                argumentIndex++;
            }

            argumentIndex = argumentIndex - 1;

            Object argValue = argValues[argumentIndex]; // argument out of range exception

            // Step 1. format the string normally
            StringBuilder sbr = new StringBuilder();
            sbr.append("%");

            // start from index 1 - no need of argument index
            for (int j = 1; j < groups.length; j++) {
                if (!TextUtils.isEmpty(groups[j])) {
                    sbr.append(groups[j]);
                }
            }

            String format = sbr.toString();
            String formattedValue = String.format(format, argValue);

            sb.replace(i, end, formattedValue);

            // Step 2. format the string
            if (argValue instanceof Spanned) {
                Spanned spanned = (Spanned) argValue;
                TextUtils.copySpansFrom(spanned, 0, spanned.length(),
                        Object.class, sb, i);
            } else {
                // get all the mentioned styles for the index
                List<CharacterStyle> applyStyles = styleArray
                        .get(argumentIndex);

                if (applyStyles != null && applyStyles.size() > 0) {
                    for (CharacterStyle style : applyStyles) {
                        sb.setSpan(style, i, i + formattedValue.length(),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
            }

            i = i + formattedValue.length();
        }

        return new SpannableString(sb);
    }

    private static SparseArray<List<CharacterStyle>> getStringStyles(
            Context context, int styleArrayId) {

        StyleToSpan styleToSpan = new StyleToSpan();
        SparseArray<List<CharacterStyle>> styleArray = new SparseArray<List<CharacterStyle>>();

        String[] styleDefinedArray = null;
        styleDefinedArray = context.getResources().getStringArray(styleArrayId);

        int i = 0;
        while (i < styleDefinedArray.length) {
            String item = styleDefinedArray[i];
            String[] split = item.split("\\|");
            int index = Integer.parseInt(split[0]);
            String individualStyle = split[1];

            List<CharacterStyle> spans = styleToSpan.getSpans(context,
                    individualStyle, "style", "com.suryar.android.stylablestring");
            styleArray.put(index, spans);
            i++;
        }

        return styleArray;
    }
}
