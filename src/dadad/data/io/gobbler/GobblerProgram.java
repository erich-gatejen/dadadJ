package dadad.data.io.gobbler;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dadad.data.io.CSVReader;
import dadad.data.model.Term;
import dadad.platform.AnnotatedException;

/**
 * Program compiler and executor.
 * <b>
 * Gobbler programs are a single instruction per line formatted in CSV.
 * See {@link GobblerInstruction} for supported instructions.
 */
public class GobblerProgram {

	enum BLOCK__OPER3 {
		IGNORE,
		EMPTY,
		ERROR,
		REST;
	}
	
	// ===============================================================================
	// = FIELDS
	
	private GobblerExecInstruction[]	instructions;
	
	// ===============================================================================
	// = METHODS
	
	public GobblerProgram(final String name, final Reader programReader) {
		
		int lineNumber = 1;
		try {
			ArrayList<GobblerExecInstruction> instructions = new ArrayList<GobblerExecInstruction>();
			BufferedReader br = new BufferedReader(programReader);
			
			String line = br.readLine();			
			while (line != null) {
				
				line = line.trim();
				if (line.length() > 0) {
				
					GobblerExecInstruction instruction = new GobblerExecInstruction();
					String[] fields = CSVReader.split(line);
					try {
						instruction.instruction = GobblerInstruction.valueOf(fields[0].trim());
								
					} catch (Exception e) {
						throw new Exception("Unknown instruction: [" + fields[0] + "]");
					}
					
					switch(instruction.instruction) {
					case CHOP:
						if (fields.length < 2) throw new Exception("Missing CHOP length.");
						instruction.operands = box(getInt(fields, 1));
						break;
						
					case BLOCK:
						if (fields.length < 2) throw new Exception("Missing START character.");
						if (fields.length < 3) throw new Exception("Missing END character.");
						fields[1] = fields[1].trim();
						if ((fields[1].length() < 1) || (fields[1].length() > 1)) throw new Exception("BLOCK oper1 must specify a single START character");
						fields[2] = fields[2].trim();
						if ((fields[2].length() < 1) || (fields[2].length() > 1)) throw new Exception("BLOCK oper2 must specify a single END character");

						String oper3 = BLOCK__OPER3.IGNORE.name();
						try {
							if (fields.length >= 4) oper3 = fields[3];
							oper3 = BLOCK__OPER3.valueOf(oper3.toUpperCase()).name();
						} catch (IllegalArgumentException e) {
							throw new AnnotatedException("Not an allowed operation")
								.annotate("oper3", oper3);
						}
									
						instruction.operands = box(fields[1], fields[2], oper3);						
						break;
						
					case SPLIT:
						if (fields.length < 2) throw new Exception("Missing SPLIT regex.");
						instruction.operands = box(fields[1].trim());							
						break;
											
					case REST:
						break;
						
					case TERM:
						break;
					}
					
					instructions.add(instruction);
				}
				
				line = br.readLine();
				lineNumber++;
			}
						
			this.instructions = instructions.toArray(new GobblerExecInstruction[instructions.size()]);
			
		} catch (Exception e) {
			throw new AnnotatedException("Gobbler program compilation failed.", AnnotatedException.Catagory.FAULT, e)
					.annotate("program.name", name, "line.number", Integer.toString(lineNumber));
		}
		
	}
	
	public List<Term> execute(final String text) {
		ArrayList<Term> result = new ArrayList<Term>();
		int ip = 0;
		// Pattern pattern = null;
		
		try {	
			int rover = 0;
			if (text.length() < 1) return null;  // Trivial
			
			outer:
			for (ip = 0; ip < instructions.length; ip++) {
				
				switch(instructions[ip].instruction) {
				
				case CHOP:					
					int chop = (Integer) instructions[ip].operands[0];
					if (chop < 0) throw new Exception("You must chop 1 or more characters.");
					if (rover >= text.length()) throw new Exception("Nothing left to CHOP");
					if ((rover + chop) > text.length()) {
						result.add(new Term(text.substring(rover), rover, text.length())); 
						ip = instructions.length;	// Quit program
					} else {
						result.add(new Term(text.substring(rover, rover + chop), rover, rover + chop)); 
						rover = rover + chop;
					}
					break;
					
				case BLOCK:
					BLOCK__OPER3 boper = BLOCK__OPER3.valueOf( ((String)instructions[ip].operands[2]).toUpperCase());

					if (rover >= text.length()) {
						if (boper == BLOCK__OPER3.ERROR) throw new Exception("No text left to process block.");
						ip = instructions.length;
						break;
					}
					
					int startBlock = text.indexOf(instructions[ip].operands[0].toString().charAt(0), rover);
					int endBlock = text.indexOf(instructions[ip].operands[1].toString().charAt(0), startBlock + 1);
					if ((startBlock < 0) || (endBlock < 0)) {
						switch(boper) {
						case IGNORE:
							continue outer;							
						case EMPTY:
							result.add(new Term("", rover, rover));
							continue outer;							
						case ERROR:
							throw new Exception("Start or end character not found.");							
						case REST:
							instructions[ip].instruction = GobblerInstruction.REST; 
							ip = instructions.length;
							continue outer;														
						}
					}

					if (endBlock > startBlock + 1)
						result.add(new Term(text.substring(startBlock + 1, endBlock), startBlock + 1, endBlock));
					else 
						result.add(new Term("", endBlock, endBlock));
					
					rover = endBlock + 1;
					break;
					
				case SPLIT:
                    if (rover < text.length()) {
                        Matcher m = Pattern.compile((String) instructions[ip].operands[0]).matcher(text.substring(rover));
                        while(m.find()) {
                            result.add(new Term(m.group(1), text.indexOf(m.group(1), rover),
                                               text.indexOf(m.group(1), rover) + m.group(1).length()));
                        }
					}
                    ip = instructions.length;
					break;
				
				case REST:
					if (rover < text.length()) result.add(new Term(text.substring(rover), rover, text.length()));
					break;
					
				case TERM:
					// Bleed off any whitespace we are currently on.
					for (; rover < text.length(); rover++) {
						if (! Character.isWhitespace(text.charAt(rover))) break;
					}
					if (rover == text.length()) {
						ip = instructions.length;
						break;
					}
					
					int walker = rover;
					for (; walker < text.length(); walker++) {
						if (Character.isWhitespace(text.charAt(walker))) break;
					}
					if (walker > rover)
						result.add(new Term(text.substring(rover, walker), rover, walker));
					else 
						result.add(new Term("", walker, walker));					
					
					rover = walker;
					break;					
				}
								
			}
		
		} catch (Exception e) {
			throw new AnnotatedException("Failed to execute.", AnnotatedException.Catagory.ERROR, e)
					.annotate("instruction.number", ip, "instruction", instructions[ip].instruction.name());
		}
		
		return result;
	}
	
	// ===============================================================================
	// = TOOLS
	
	private Long getLong(final String[] fields, final int offset) {
		try {
			return Long.parseLong(fields[offset]);
		} catch(Exception e) {
			throw new AnnotatedException("Failed to get Long due to number formating error", e)
				.annotate("operand.number", offset, "text", fields[offset]);
		}
	}
	
	private Integer getInt(final String[] fields, final int offset) {
		try {
			return Integer.parseInt(fields[offset]);
		} catch(Exception e) {
			throw new AnnotatedException("Failed to get Integer due to number formating error", e)
				.annotate("operand.number", offset, "text", fields[offset]);
		}
	}
	
	private Object[] box(Object... objects) {
		return objects;
	}

}
