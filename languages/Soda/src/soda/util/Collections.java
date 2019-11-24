package soda.util;

import groovy.transform.CompileStatic;
import hlaaftana.soda.operators.Operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CompileStatic
public class Collections {
	public static <K, V> Map<V, K> flip(Map<K, V> map) {
		Map<V, K> newMap = new HashMap<>();
		for (Map.Entry<K, V> e : map.entrySet()) {
			newMap.put(e.getValue(), e.getKey());
		}
		return newMap;
	}

	public static int sum(int[] arr) {
		int result = 0;
		for (int i : arr) result += i;
		return result;
	}

	public static long sum(long[] arr) {
		long result = 0L;
		for (long i : arr) result += i;
		return result;
	}

	public static float sum(float[] arr) {
		float result = 0f;
		for (float i : arr) result += i;
		return result;
	}

	public static double sum(double[] arr) {
		double result = 0d;
		for (double i : arr) result += i;
		return result;
	}

	public static short sum(short[] arr) {
		short result = (short) 0;
		for (short i : arr) result += i;
		return result;
	}

	public static char sum(char[] arr) {
		char result = (char) 0;
		for (char i : arr) result += i;
		return result;
	}

	public static byte sum(byte[] arr) {
		byte result = (byte) 0;
		for (byte i : arr) result += i;
		return result;
	}

	public static BigInteger sum(BigInteger[] arr) {
		BigInteger result = BigInteger.ZERO;
		for (BigInteger i : arr) result = result.add(i);
		return result;
	}

	public static BigDecimal sum(BigDecimal[] arr) {
		BigDecimal result = BigDecimal.ZERO;
		for (BigDecimal i : arr) result = result.add(i);
		return result;
	}

	/*public static <T extends Number> T sum(List<T> numbers) {
		return sum((T[]) numbers.toArray());
	}*/
}
