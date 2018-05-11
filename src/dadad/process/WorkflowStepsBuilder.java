package dadad.process;

import java.util.HashMap;
import java.util.LinkedList;
/*
public class WorkflowStepsBuilder<EE> {
		
	 LinkedList<WorkflowStep<EE, ?>> steps = new LinkedList<WorkflowStep<EE, ?>>();
	 
	 public WorkflowStepBuilder<EE> add(WorkflowStep<EE, ?> step) {
		 steps.add(step);
		 return this;
	 }
	
	 public List<WorkflowStep<EE, ?>> build() {
		 return steps;
	 }
}
*/
public class WorkflowStepsBuilder {
	
	 LinkedList<WorkflowStep<?, ?>> steps = new LinkedList<WorkflowStep<?, ?>>();
	 HashMap<String, Integer> positionByName = new HashMap<String, Integer>();
	 int position = 0;
	 
	 public WorkflowStepsBuilder add(final WorkflowStep<?, ?> step) {
		 steps.add(step);
		 position++;
		 return this;
	 }
	
	 public WorkflowStepsBuilder add(final WorkflowStep<?, ?> step, final String name) {
		 positionByName.put(name, position);
		 return add(step);
	 }
	 
	 public WorkflowStep<?, ?>[] build() {
		 return steps.toArray(new WorkflowStep<?, ?>[steps.size()]);
	 }
	 	 
	 public int getPositionByName(final String name) {
		 Integer position = positionByName.get(name);
		 if (position == null) throw new RuntimeException("Step name does not have a position.  name=" + name);
		 return position;
	 }
	 
	 public int size() {
		 return steps.size();
	 }
}
