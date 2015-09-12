package com.suryar.android.stylablestring;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

/**
 * Given a style, convert to span
 */
public class StyleToSpan {

	static int DEFAULT_VALUE = -1;

	static int[] attrs = { android.R.attr.textSize, android.R.attr.textColor };

	public List<CharacterStyle> getSpans(Context context, String styleName,
			String defType, String defPackage) {
		
		if(context == null) {
			throw new IllegalArgumentException("context is null");
		}
		
		if(TextUtils.isEmpty(styleName)) {
			throw new IllegalArgumentException("context is null");
		}
		
		if(TextUtils.isEmpty(defType)) {
			throw new IllegalArgumentException("defType");
		}
		
		if(TextUtils.isEmpty(defPackage)) {
			throw new IllegalArgumentException("defPackage");
		}
		
		List<CharacterStyle> result = new LinkedList<CharacterStyle>();

		int styleId = context.getResources().getIdentifier(styleName, defType,
				defPackage);

		TypedArray styleAttributes = null;

		if (styleId != 0) {
			try {
				styleAttributes = context
						.obtainStyledAttributes(styleId, attrs);
				
				int mTextSize = styleAttributes.getDimensionPixelSize(0, -1);
				int mTextColor = styleAttributes.getColor(1, DEFAULT_VALUE);

				if (mTextSize != DEFAULT_VALUE) {
					result.add(new AbsoluteSizeSpan(mTextSize));
				}
				if (mTextColor != DEFAULT_VALUE) {
					result.add(new ForegroundColorSpan(mTextColor));
				}
			} catch (NotFoundException ex) {
				Log.e(StyleToSpan.class.getName(),
						String.format("style not found {0}", styleName));
			} finally {
				if (styleAttributes != null) {
					styleAttributes.recycle();
				}
			}
		}

		return result;
	}
}