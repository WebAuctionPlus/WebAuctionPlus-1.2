package com.poixson.commonjava.Utils;



public class utilsString {
	private utilsString() {}



	// repeat string with deliminator
	public static String repeat(final int count, final String str) {
		return repeat(count, str, null);
	}
	public static String repeat(final int count, final String str, final String delim) {
		if(utils.isEmpty(str)) throw new NullPointerException("str cannot be null");
		if(count < 1) return "";
		final StringBuilder out = new StringBuilder();
		// repeat string
		if(utils.isEmpty(delim)) {
			for(int i = 0; i < count; i++)
				out.append(str);
		} else {
			// repeat string with delim
			boolean b = false;
			for(int i = 0; i < count; i++) {
				if(b) out.append(delim);
				b = true;
				out.append(str);
			}
		}
		return out.toString();
	}
	public static String repeat(final int count, final char chr) {
		if(count < 1) return "";
		final StringBuilder out = new StringBuilder();
		// repeat string
		for(int i = 0; i < count; i++)
			out.append(chr);
		return out.toString();
	}



	public static String pad(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final int count = width - text.length();
		if(count < 1) return text;
		return (new StringBuilder(width))
			.append(text)
			.append(repeat(count, padding))
			.toString();
	}
	public static String padFront(final int width, final String text, final char padding) {
		if(width < 1) return null;
		final int count = width - text.length();
		if(count < 1) return text;
		return (new StringBuilder(width))
			.append(repeat(count, padding))
			.append(text)
			.toString();
	}
	public static String padCenter(final int width, final String text, final char padding) {
		if(width < 1) return null;
		if(utils.isEmpty(text))
			return repeat(width, padding);
		final double count = ( ((double) width) - ((double) text.length()) ) / 2.0;
		if(Math.ceil(count) < 1.0) return text;
		return (new StringBuilder(width))
			.append(repeat((int) Math.floor(count), padding))
			.append(text)
			.append(repeat((int) Math.ceil(count), padding))
			.toString();
	}



}
