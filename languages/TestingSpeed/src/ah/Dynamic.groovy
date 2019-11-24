package ah

class Dynamic {
	static boolean test(){
		int[] list = new int[50]
		for (int i = 0; i < 50; ++i)
			list[i] = sum(fibonacciSequence(200))
		areAllEqual(list)
	}

	static int[] fibonacciSequence(int max){
		int[] seq = new int[max < 2 ? 2 : max]
		seq[0] = seq[1] = 1
		for (int i = 2; i < max; ++i){
			seq[i] = seq[i - 2] + seq[i - 1]
		}
		seq
	}

	static int sum(int[] array){
		int result = 0
		for (int i = 0; i < array.length; ++i) result += array[i]
		return result
	}

	static boolean areAllEqual(array){
		def lastObject = array[0]
		for (int i = 0; i < array.length; ++i){
			if (array[i] == lastObject) lastObject = array[i]
			else return false
		}
		true
	}
}
