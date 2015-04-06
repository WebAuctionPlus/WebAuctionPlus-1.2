package com.poixson.commonjava.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;


public final class utils {
	private utils() {}



	/**
	 * Is string empty.
	 * @param String
	 * @return True if string is null or empty.
	 */
	public static boolean isEmpty(final String value) {
		return (value == null || value.length() == 0);
	}
	/**
	 * Is string populated.
	 * @param String
	 * @return True if string is not null or contains data.
	 */
	public static boolean notEmpty(final String value) {
		return (value != null && value.length() > 0);
	}



	/**
	 * Is array empty.
	 * @param Object[]
	 * @return True if array is null or empty.
	 */
	public static boolean isEmpty(final Object[] array) {
		return (array == null || array.length == 0);
	}
	/**
	 * Is array populated.
	 * @param Object[]
	 * @return True if array is not null or contains data.
	 */
	public static boolean notEmpty(final Object[] array) {
		return (array != null && array.length > 0);
	}



	/**
	 * Is collection/set/list empty.
	 * @param Collection or Set or List
	 * @return True if collection is null or empty.
	 */
	public static boolean isEmpty(final Collection<?> collect) {
		return (collect == null || collect.isEmpty());
	}
	/**
	 * Is collection/set/list populated.
	 * @param Collection or Set or List
	 * @return True if collection is not null or contains data.
	 */
	public static boolean notEmpty(final Collection<?> collect) {
		return (collect != null && !collect.isEmpty());
	}



	/**
	 * Is map empty.
	 * @param Map
	 * @return True if map is null or empty.
	 */
	public static boolean isEmpty(final Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}
	/**
	 * Is map populated.
	 * @param Map
	 * @return True if map is not null or contains data.
	 */
	public static boolean notEmpty(final Map<?, ?> map) {
		return (map != null && !map.isEmpty());
	}



	/**
	 * Close safely, ignoring errors.
	 */
	public static void safeClose(Closeable obj) {
		if(obj == null) return;
		try {
			obj.close();
		} catch (IOException ignore) {}
	}



	/**
	 * Current system time ms.
	 * @return
	 */
	public static long getSystemMillis() {
		return System.currentTimeMillis();
	}



	public static void ListProperties() {
		final Properties props = System.getProperties();
		props.list(System.out);
	}



	public static boolean isLibAvailable(final String classpath) {
		try {
			Class.forName(classpath);
			return true;
		} catch (ClassNotFoundException ignore) {}
		return false;
	}
	public static boolean isJLineAvailable() {
		return isLibAvailable("jline.Terminal");
	}
	public static boolean isRxtxAvailable() {
		return isLibAvailable("gnu.io.CommPortIdentifier");
	}



	public static boolean validBaud(final int baud) {
		switch(baud) {
		case 300:
		case 1200:
		case 2400:
		case 4800:
		case 9600:
		case 14400:
		case 19200:
		case 28800:
		case 38400:
		case 57600:
		case 115200:
			return true;
		default:
		}
		return false;
	}



	// compare versions
	public static String compareVersions(final String versionA, final String versionB) {
		if(utils.isEmpty(versionA) || utils.isEmpty(versionB))
			throw new NullPointerException();
		final String[] norms = normalisedVersions(versionA, versionB);
		final int cmp = norms[0].compareTo(norms[1]);
		if(cmp < 0)
			return "<";
		if(cmp > 0)
			return ">";
		return "=";
	}
	private static String[] normalisedVersions(final String versionA, final String versionB) {
		if(utils.isEmpty(versionA)) throw new NullPointerException();
		if(utils.isEmpty(versionB)) throw new NullPointerException();
		// split string by .
		final String[] splitA = Pattern.compile(".", Pattern.LITERAL).split(versionA);
		final String[] splitB = Pattern.compile(".", Pattern.LITERAL).split(versionB);
		if(utils.isEmpty(splitA)) throw new NullPointerException();
		if(utils.isEmpty(splitB)) throw new NullPointerException();
		// find longest part
		int width = -1;
		for(final String part : splitA) {
			if(width == -1 || width < part.length())
				width = part.length();
		}
		for(final String part : splitB) {
			if(width == -1 || width < part.length())
				width = part.length();
		}
		if(width == -1) throw new NullPointerException();
		// build padded string
		final StringBuilder outA = new StringBuilder();
		for(final String part : splitA) {
			outA.append( utilsString.padFront(width, part, '0') );
		}
		final StringBuilder outB = new StringBuilder();
		for(final String part : splitB) {
			outB.append( utilsString.padFront(width, part, '0') );
		}
		return new String[] {
			outA.toString(),
			outB.toString()
		};
	}
	public static boolean checkJavaVersion(final String requiredVersion) {
		final String javaVersion;
		{
			final String vers = System.getProperty("java.version");
			if(vers == null || vers.isEmpty()) throw new NullPointerException("Failed to get java version");
			javaVersion = vers.replace('_', '.');
		}
		return !(compareVersions(javaVersion, requiredVersion).equals("<"));
	}



}
