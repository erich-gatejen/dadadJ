package dadad.data.io.gobbler;

/**
 * Gobbler instruction
 *
 */
public enum GobblerInstruction {
	
	/**
	 * Chop the exact number of characters from the current position.
	 * OPER 1: number of characters to chop.  If there aren't enough left, it will yield what it can.
	 */
	CHOP,
	
	/**
	 * A delimited block
	 * OPER1: Single character signifying the start of the block
	 * OPER2: Single character signifying the end of the block
	 * OPER3: OPTIONAL ignore, error, empty, rest
	 * 			ignore = skip if the opening character is not found.  (This is the DEFAULT)
	 * 			error = error if the opening character is not found.
	 * 			empty = yield an empty field if the opening character is not found.
	 * 			rest = yield all remaining characters as a field and quit program.
	 * 
	 * IF there is no more text left to look for the BLOCK, it will quietly end the program, unless the OPER3=error.  
	 * In that case, it will throw and exception.
	 */
	BLOCK,
	
	/**
	 * Split using the given regex.  Currently, only one SPLIT is allowed and it will consume the rest of the line.
	 */
	SPLIT,
	
	/**
	 * Take the rest of the input as a single field.  It will be trimmed.
	 */
	REST,
	
	/**
	 * Get the next whitespace delimited term.	
	 */
	TERM;
}
