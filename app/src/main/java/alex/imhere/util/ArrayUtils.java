package alex.imhere.util;

import java.util.List;

public class ArrayUtils {
	static public int[] ToInts(List<Integer> list) {
		int[] ints = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ints[i] = list.get(i);
		}
		return ints;
	}
}
