package com.poixson.commonjava.Utils;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Pattern;


public final class utilsNumbers {
	private utilsNumbers() {}



	// true;
	public static final String[] TRUE_VALUES = new String[] {
		"true",
		"enabled",
		"yes",
		"on",
	};
	public static final char[] T_VALUES = new char[] {
		'1',
		't',
		'e',
		'y'
	};
	// false
	public static final String[] FALSE_VALUES = new String[] {
		"false",
		"disabled",
		"no",
		"off"
	};
	public static final char[] F_VALUES = new char[] {
		'0',
		'f',
		'd',
		'n'
	};



	// parse number
	public static Integer toInteger(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Integer(Integer.parseInt(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static int toInteger(final String value, final int def) {
		final Integer num = toInteger(value);
		if(num == null) return def;
		return num.intValue();
	}

	// parse byte
	public static Byte toByte(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Byte(Byte.parseByte(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static byte toByte(final String value, final byte def) {
		final Byte num = toByte(value);
		if(num == null) return def;
		return num.byteValue();
	}

	// parse short
	public static Short toShort(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Short(Short.parseShort(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static short toShort(final String value, final short def) {
		final Short num = toShort(value);
		if(num == null) return def;
		return num.shortValue();
	}

	// parse long
	public static Long toLong(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Long(Long.parseLong(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static long toLong(final String value, final long def) {
		final Long num = toLong(value);
		if(num == null) return def;
		return num.longValue();
	}

	// parse double
	public static Double toDouble(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Double(Double.parseDouble(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static double toDouble(final String value, final double def) {
		final Double num = toDouble(value);
		if(num == null) return def;
		return num.doubleValue();
	}

	// parse float
	public static Float toFloat(final String value) {
		if(utils.isEmpty(value)) return null;
		try {
			return new Float(Float.parseFloat(value));
		} catch (NumberFormatException ignore) {}
		return null;
	}
	public static float toFloat(final String value, final float def) {
		final Float num = toFloat(value);
		if(num == null) return def;
		return num.floatValue();
	}

	// parse boolean
	public static Boolean toBoolean(final String value) {
		if(utils.isEmpty(value)) return null;
		final String val = value.trim().toLowerCase();
		for(final String v : TRUE_VALUES)
			if(val.equals(v))
				return Boolean.TRUE;
		for(final String v : FALSE_VALUES)
			if(val.equals(v))
				return Boolean.FALSE;
		final char chr = val.charAt(0);
		for(final char c : T_VALUES)
			if(chr == c)
				return Boolean.TRUE;
		for(final char c : F_VALUES)
			if(chr == c)
				return Boolean.FALSE;
		return null;
	}
	public static boolean toBoolean(final String value, final boolean def) {
		final Boolean bool = toBoolean(value);
		if(bool == null) return def;
		return bool.booleanValue();
	}



	// is number
	public static boolean isNumeric(final String value) {
		if(utils.isEmpty(value)) return false;
		return (toLong(value) != null);
	}
	// is boolean
	public static boolean isBoolean(final String value) {
		return (toBoolean(value) != null);
	}



	// formatDecimal("0.00", double)
	public static String FormatDecimal(final String format, final double value) {
		return (new DecimalFormat(format).format(value));
	}
	// formatDecimal("0.00", float)
	public static String FormatDecimal(final String format, final float value) {
		return (new DecimalFormat(format).format(value));
	}



	public static double round(final double value, final double product) {
		final double prod = (double) product;
		final double val = (double) ((long) Math.round( value / prod ));
		return val * prod;
	}
	public static double floor(final double value, final double product) {
		final double prod = (double) product;
		final double val = (double) ((long) Math.floor( value / prod ));
		return val * prod;
	}
	public static double ceil(final double value, final double product) {
		final double prod = (double) product;
		final double val = (double) ((long) Math.ceil( value / prod ));
		return val * prod;
	}



	public static int round(final int value, final int product) {
		return (int) round(
				(double) value,
				(double) product
		);
	}
	public static int floor(final int value, final int product) {
		return (int) floor(
				(double) value,
				(double) product
		);
	}
	public static int ceil(final int value, final int product) {
		return (int) ceil(
				(double) value,
				(double) product
		);
	}



	public static long round(final long value, final int product) {
		return (long) round(
				(double) value,
				(double) product
		);
	}
	public static long floor(final long value, final int product) {
		return (long) floor(
				(double) value,
				(double) product
		);
	}
	public static long ceil(final long value, final int product) {
		return (long) ceil(
				(double) value,
				(double) product
		);
	}



	// min/max value
	public static int MinMax(final int value, final int min, final int max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static byte MinMax(final byte value, final byte min, final byte max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static short MinMax(final short value, final short min, final short max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static long MinMax(final long value, final long min, final long max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static double MinMax(final double value, final double min, final double max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
	public static float MinMax(final float value, final float min, final float max) {
		if(min > max) throw new IllegalArgumentException("min cannot be greater than max");
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}



	// is within range
	public static boolean isMinMax(final int value, final int min, final int max) {
		return (value == MinMax(value, min, max));
	}
	public static boolean isMinMax(final byte value, final byte min, final byte max) {
		return (value == MinMax(value, min, max));
	}
	public static boolean isMinMax(final short value, final short min, final short max) {
		return (value == MinMax(value, min, max));
	}
	public static boolean isMinMax(final long value, final long min, final long max) {
		return (value == MinMax(value, min, max));
	}
	public static boolean isMinMax(final double value, final double min, final double max) {
		return (value == MinMax(value, min, max));
	}
	public static boolean isMinMax(final float value, final float min, final float max) {
		return (value == MinMax(value, min, max));
	}



	// compare version numbers
	public static String compareVersions(final String oldVersion, final String newVersion) {
		if(utils.isEmpty(oldVersion)) return null;
		if(utils.isEmpty(newVersion)) return null;
		final int cmp = normalisedVersion(oldVersion).compareTo(normalisedVersion(newVersion));
		if(cmp < 0) return "<";
		if(cmp > 0) return ">";
		return "=";
		//return cmp<0 ? "<" : cmp>0 ? ">" : "=";
	}
	public static String normalisedVersion(final String version) {
		final String delim = ".";
		final int maxWidth = 5;
		final StringBuilder str = new StringBuilder();
		final String[] split = Pattern.compile(delim, Pattern.LITERAL).split(version);
		for(final String part : split)
			str.append(String.format("%"+maxWidth+'s', part));
		return str.toString();
	}



	// random number
	public static int getRandom(final int minNumber, final int maxNumber) {
		final Random gen = new Random(utils.getSystemMillis());
		return gen.nextInt(maxNumber - minNumber) + minNumber;
	}
	// random number (not last)
	public static int getNewRandom(final int minNumber, final int maxNumber, final int oldNumber) {
		if(minNumber == maxNumber)
			return minNumber;
		if((maxNumber - minNumber) == 1) {
			if(oldNumber == minNumber)
				return maxNumber;
			return minNumber;
		}
		int newNumber;
		for(int i=0; i<100; i++) {
			newNumber = getRandom(minNumber, maxNumber);
			if(newNumber != oldNumber) return newNumber;
		}
		throw new IllegalAccessError("Failed to generate a random number");
	}



}
