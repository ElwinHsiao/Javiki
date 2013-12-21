package norg.javiki;

import java.util.Random;

public class IdentityComputer {
//	public static int computeId(Object... inputs) {
//		if (inputs == null || inputs.length == 0) {
//			return new Random().nextInt();
//		}
//		int hash[] = new int[inputs.length];
//		for (int i = 0; i < inputs.length; ++i) {
//			hash[i] = inputs[i].hashCode();
//		}
//		return hash.hashCode();
//	}
	
	public static long computeId(Object... inputs) {
		long randomLong = new Random().nextLong();
		if (inputs == null || inputs.length == 0) {
			return randomLong;
		}
		int hash[] = new int[inputs.length];
		for (int i = 0; i < inputs.length; ++i) {
			hash[i] = inputs[i].hashCode();
		}
		return randomLong + hash.hashCode();
	}
}
