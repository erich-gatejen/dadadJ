package dadad.platform;

import java.util.Random;

public class RNG {

	// ===============================================================================
	// = FIELDS

	public final Random rng;
	

	// ===============================================================================
	// = METHOD
	
	public RNG() {
		rng = new Random();
	} 
	
	public boolean percent(final int  chance) {
		if (chance == 100) return true;
		if ((chance < 1) || (chance > 100)) return false;
		if ((rng.nextInt(100) + 1) <= chance) return true;
		return false;
	}
	
	public int range(final int lowest, final int highest) {
		return lowest + rng.nextInt(highest - lowest);
	}
	
	public String pick(final String[]  strings) {
		if ((strings==null) || (strings.length < 1)) return null;
		return strings[rng.nextInt(strings.length)];
	}

	public int pickInteger(final int[]  numbers) {
		if ((numbers==null)||(numbers.length <1)) return 0;
		return numbers[rng.nextInt(numbers.length)];
	}
	    
}

