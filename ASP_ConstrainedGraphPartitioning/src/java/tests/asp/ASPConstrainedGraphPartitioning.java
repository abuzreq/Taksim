package asp;
import java.io.File;
import java.util.List;

import asp4j.lang.AnswerSet;
import asp4j.program.Program;
import asp4j.program.ProgramBuilder;
import asp4j.solver.Solver;
import asp4j.solver.SolverBase;
import asp4j.solver.SolverClingo;
import asp4j.solver.object.Binding;
import asp4j.solver.object.Filter;
import asp4j.solver.object.ObjectSolver;
import asp4j.solver.object.ObjectSolverImpl;
import search.basic.GraphPartitioningState;

/**
 *
 * @author hbeck Jun 9, 2013
 */
public class ASPConstrainedGraphPartitioning
{
	private final static String rulefile_main = System.getProperty("user.dir") + "/src/java/tests/files/partition.lp";

	private String[] extraRuleFiles;
	private boolean allowNodeRemoval;
	private int timeLimit,numModels;
	
	public ASPConstrainedGraphPartitioning(int numNodes,int numPartitions, boolean allowNodeRemoval, int timeLimit, int numModels,String...rulefiles)
	{
		this.extraRuleFiles = rulefiles;
		this.numNodes = numNodes;
		this.numPartitions = numPartitions;
		this.allowNodeRemoval = allowNodeRemoval;
		this.timeLimit = timeLimit;
		this.numModels = numModels;
	}
	public List<String> getAnswerSet()
	{
		try {
			return getAnswerSet(new SolverClingo(numModels), rulefile_main,extraRuleFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private int numNodes ;
	private int numPartitions;
	private List<String> getAnswerSet(SolverBase externalSolver,String mainRuleFile, String[] extraRuleFiles) throws Exception 
	{
		ObjectSolverImpl solver = new ObjectSolverImpl(externalSolver);
		externalSolver.setExtraParams(" --const n="+numNodes+" --const p="+numPartitions + " --rand-freq=0.5" + " --const r="+(allowNodeRemoval?0:1) + " --time-limit="+timeLimit);//+" -t 4,compete"
		ProgramBuilder<Object> pb = new ProgramBuilder<>();
		
		pb.add(new File(mainRuleFile));
		for(String rulefile : extraRuleFiles)
		{
			pb.add(new File(rulefile));
		}
		
		
		Program<Object> program = pb.build();
		
		Class[] classes = {String.class,Node.class,Edge.class,Reach.class,Belongs.class,Contains.class,Contagious.class,Family.class,Partition.class,QuotientEdge.class};
		Filter filter = new Filter();
		Binding binding = new Binding();

		for (int i = 0 ; i < classes.length;i++)
		{
			filter.add(classes[i]);
			//binding.add(classes[i]);
		}
		
		List<String> answerSets = solver.computeAnswerSetsAsStrings(program,binding,filter);
		
		return answerSets;	
	}


	
	

	
}
