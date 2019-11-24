package ah;

public class Java {
	public static boolean test(){
		int[] array = new int[50];
		for (int i = 0; i < 50; ++i){
			array[i] = sum(fibonacciSequence(200));
		}
		return areAllEqual(array);
	}

	public static int sum(int[] array){
		int result = 0;
		for (int i = 0; i < array.length; ++i) result += array[i];
		return result;
	}

	public static int[] fibonacciSequence(int max){
		int[] seq = new int[max < 2 ? 2 : max];
		seq[0] = seq[1] = 1;
		for (int i = 2; i < max; ++i) seq[i] = seq[i - 2] + seq[i - 1];
		return seq;
	}

	public static boolean areAllEqual(int[] array){
		int lastObject = array[0];
		for (int i = 0; i < array.length; ++i){
			if (array[i] == lastObject) lastObject = array[i];
			else return false;
		}
		return true;
	}
}
