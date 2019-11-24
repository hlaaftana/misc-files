package hlaaftana.oldbutnotvery.kismet.scope;

import java.util.HashMap;
import java.util.Map;

public class StringEscaper {
	private static final Map<Character, String> to = new HashMap<>();
	private static final Map<String, Character> from;

	static {
		to.put('\\', "\\\\"); to.put('"', "\\\""); to.put('\'', "\\\'"); to.put('\b', "\\b");
		to.put('\n', "\\n"); to.put('\t', "\\t"); to.put('\f', "\\f"); to.put('\r', "\\r");
		to.put('\u000b', "\\v"); to.put('\7', "\\a"); to.put('\u001b', "\\b");
		from = flip(to);
	}

	static <K, V> Map<V, K> flip(Map<K, V> map) {
		Map<V, K> newMap = new HashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			newMap.put(e.getValue(), e.getKey());
		}
		return newMap;
	}

	public static String escapeSoda(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i : str.codePoints().toArray()) {
			if (i < 93 && to.containsKey((char) i)) builder.append(to.get((char) i));
			else if (i > 255) builder.append("\\u{").append(Integer.toString(i, 16)).append('}');
			else builder.appendCodePoint(i);
		}
		return builder.toString();
	}

	public static String unescapeSoda(String str) throws NumberFormatException {
		StringBuilder builder = new StringBuilder();
		boolean escaped = false;
		boolean recordU = false;
		int uBase = 0;
		StringBuilder u = null;
		for (char c : str.toCharArray()) {
			if (escaped) {
				if (null != u) {
					if (recordU)
						if (c == '}') {
							recordU = false;
							builder.appendCodePoint(Integer.parseInt(u.toString(), uBase));
							u = null;
						} else u.append(c);
					else if (c == 'x' || c == 'X') uBase = 16;
					else if (c == 'o' || c == 'O') uBase = 8;
					else if (c == 'd' || c == 'D') uBase = 10;
					else if (c == 'b' || c == 'B') uBase = 2;
					else if (c == '{') {
						if (uBase < 2) uBase = 16;
						recordU = true;
					} else {
						recordU = false;
						builder.append('\\').append('u').append(c);
						u = null;
					}
					continue;
				} else {
					if (c == 'u') { u = new StringBuilder(); continue; }
					else {
						String esc = "\\" + c;
						if (from.containsKey(esc)) {
							builder.deleteCharAt(builder.length() - 1);
							builder.append(from.get(esc));
						}
						else builder.append(c);
					}
				}
			} else builder.append(c);
			escaped = c == '\\';
		}
		return builder.toString();
	}
}
