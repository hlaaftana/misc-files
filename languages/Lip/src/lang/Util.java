package lang;

import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator;

public class Util {
	public static <K, V> Map<V, K> flip(Map<K, V> map) {
		Map<V, K> newMap = new HashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) newMap.put(e.getValue(), e.getKey());
		return newMap;
	}

	private static final Map<Character, String> to = new HashMap<>();
	private static final Map<String, Character> from;

	static {
		to.put('\\', "\\\\"); to.put('"', "\\\""); to.put('\'', "\\\'"); to.put('\b', "\\b");
		to.put('\n', "\\n"); to.put('\t', "\\t"); to.put('\f', "\\f"); to.put('\r', "\\r");
		from = flip(to);
	}

	public static String escape(String str) {
		StringBuilder builder = new StringBuilder();
		PrimitiveIterator.OfInt iter = str.codePoints().iterator();
		while (iter.hasNext()) {
			int i = iter.nextInt();
			if (i < 93 && to.containsKey((char) i)) builder.append(to.get((char) i));
			else if (i > 255) builder.append("\\u{").append(Integer.toString(i, 16)).append('}');
			else builder.appendCodePoint(i);
		}
		return builder.toString();
	}

	public static String unescape(String str) throws NumberFormatException {
		StringBuilder builder = new StringBuilder();
		boolean escaped = false;
		boolean recordU = false;
		int uBase = 0;
		StringBuilder u = null;
		PrimitiveIterator.OfInt iter = str.codePoints().iterator();
		while (iter.hasNext()) {
			int i = iter.nextInt();
			if (escaped) {
				if (null != u) {
					if (recordU) {
						if (i == '}') {
							recordU = false;
							builder.appendCodePoint(Integer.parseInt(u.toString(), uBase));
							u = null;
						} else u.appendCodePoint(i);
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
					continue;
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
