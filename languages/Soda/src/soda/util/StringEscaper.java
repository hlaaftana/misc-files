package soda.util;

import java.util.HashMap;
import java.util.Map;

public class StringEscaper {
	private static final Map<Character, String> to = new HashMap<>();
	private static final Map<String, Character> from;

	static {
		to.put('\\', "\\\\"); to.put('"', "\\\""); to.put('\'', "\\\'"); to.put('\b', "\\b");
		to.put('\n', "\\n"); to.put('\t', "\\t"); to.put('\f', "\\f"); to.put('\r', "\\r");
		from = Collections.flip(to);
	}

	public static String escapeJava(String str) {
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray()) {
			if (to.containsKey(c)) builder.append(to.get(c));
			else if (c < 32) builder.append("\\").append(Integer.toString((int) c, 8));
			else if (c > 255) builder.append("\\u").append(CharSeqUtils.lpad(Integer.toString((int) c, 16), 4, '0'));
			else builder.append(c);
		}
		return builder.toString();
	}

	public static String unescapeJava(String str) {
		StringBuilder builder = new StringBuilder();
		boolean escaped = false;
		StringBuilder u = null;
		StringBuilder o = null;
		for (char c : str.toCharArray()) {
			if (escaped) {
				if (null == o && c > 47 && c < 56) o = new StringBuilder();
				if (null != u) {
					if (u.length() < 4) u.append(c);
					if (u.length() == 4) {
						builder.append((char) Integer.parseInt(u.toString(), 16));
						u = null;
					}
					continue;
				} else if (null != o) {
					if (c > 47 && c < 56 && (o.toString() + c).compareTo("377") < 1)
						o.append(c);
					else {
						builder.append((char) Integer.parseInt(o.toString(), 8));
						o = null;
						builder.append(c);
					}
					continue;
				} else {
					if (c == 'u') u = new StringBuilder();
					else {
						String esc = "\\" + c;
						if (from.containsKey(esc)) builder.append(from.get(esc));
						else builder.append(c);
					}
				}
			} else builder.append(c);
			escaped = c == '\\';
		}
		return builder.toString();
	}

	public static String escapeSoda(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i : (Iterable<Integer>) () -> str.codePoints().iterator()) {
			if (i < 93 && to.containsKey((char) i)) builder.append(to.get((char) i));
			else if (i > 255) builder.append("\\u{").append(Integer.toString(i, 16)).append('}');
			else builder.appendCodePoint(i);
		}
		return builder.toString();
	}

	public static String unescapeSoda(String str) {
		StringBuilder builder = new StringBuilder();
		boolean escaped = false;
		boolean recordU = false;
		int uBase = 0;
		StringBuilder u = new StringBuilder();
		for (int i : (Iterable<Integer>) () -> str.codePoints().iterator()) {
			if (escaped) {
				if (null != u) {
					if (recordU) {

					} else {
						if (i == 'x' || i == 'X') uBase = 16;
						else if (i == 'o' || i == 'O') uBase = 8;
						else if (i == 'd' || i == 'D') uBase = 10;
						else if (i == 'b' || i == 'B') uBase = 2;
						else if (i == '{') {
							if (uBase < 2) uBase = 16;
							recordU = true;
						}else throw new IllegalArgumentException("Unknown unicode character base for codepoint " + i);
					}
				} else {
					if (i == 'u') u = new StringBuilder();
					else {
						StringBuilder escBuilder = new StringBuilder("\\");
						escBuilder.appendCodePoint(i);
						String esc = escBuilder.toString();
						if (from.containsKey(esc)) builder.append(from.get(esc));
						else builder.appendCodePoint(i);
					}
				}
			} else builder.appendCodePoint(i);
			escaped = i == '\\';
		}
		return builder.toString();
	}
}
