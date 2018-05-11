package dadad.process.data.wf;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;

import dadad.data.config.WorkflowConfiguration;
import dadad.data.io.CSVReader;
import dadad.platform.AnnotatedException;

/**
 * Program compiler and executor.
 *
 */
public class ProcessorProgram {
	
	// ===============================================================================
	// = FIELDS
	
	ProcessorExecInstruction[]	instructions;
	
	
	// ===============================================================================
	// = METHODS
	
	public ProcessorProgram(final String name, final Reader programReader) {
		
		int lineNumber = 1;
		try {
			ArrayList<ProcessorExecInstruction> instructions = new ArrayList<ProcessorExecInstruction>();
			BufferedReader br = new BufferedReader(programReader);
			
			String line = br.readLine();			
			while (line != null) {
				
				line = line.trim();
				if (line.length() > 0) {
				
					ProcessorExecInstruction instruction = new ProcessorExecInstruction();
					String[] fields = CSVReader.split(line);
					try {
						instruction.instruction = ProcessorInstruction.valueOf(fields[0].trim());
								
					} catch (Exception e) {
						throw new Exception("Unknown instruction: [" + fields[0] + "]");
					}

					instructions.add(instruction);
				}
				
				line = br.readLine();
				lineNumber++;
			}
						
			this.instructions = instructions.toArray(new ProcessorExecInstruction[instructions.size()]);
			
		} catch (Exception e) {
			throw new AnnotatedException("Processor program compilation failed.", AnnotatedException.Catagory.FAULT, e)
					.annotate(WorkflowConfiguration.PROCESSOR__PROGRAM_FILE.property(), name, "line.number", Integer.toString(lineNumber));
		}
		
	}
	
}
