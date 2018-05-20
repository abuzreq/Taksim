package asp;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jgrapht.graph.SimpleGraph;

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
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import util.GraphUtil;

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

	private SimpleGraph<Node, Border> G;

	private GraphPartitioningState C;
	
	public ASPConstrainedGraphPartitioning(SimpleGraph<Node, Border> G, GraphPartitioningState C, boolean allowNodeRemoval, int timeLimit, int numModels,String...rulefiles)
	{
		this.extraRuleFiles = rulefiles;
		this.G = G;
		this.C = C;
		
		this.numNodes = GraphUtil.sizeOf(G);
		this.numPartitions = GraphUtil.sizeOf(C);
		
		this.allowNodeRemoval = allowNodeRemoval;
		this.timeLimit = timeLimit;
		this.numModels = numModels;
	}
	private List<String> getAnswerSet()
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
		externalSolver.setExtraParams(" --const n="+numNodes+" --const p="+numPartitions + " --rand-freq=0.5" + " --const r="+(allowNodeRemoval?0:1) + " --time-limit="+timeLimit +" -t 4,split");//+
		ProgramBuilder<Object> pb = new ProgramBuilder<>();
		
		pb.add(new File(mainRuleFile));
		for(String rulefile : extraRuleFiles)
		{
			pb.add(new File(rulefile));
		}
		
		
		Program<Object> program = pb.build();
		
		Class[] classes = {String.class,Node.class,Edge.class,Reach.class,Belongs.class,Contains.class,Contiguous.class,Family.class,Partition.class,QuotientEdge.class};
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
	private static GraphPartitioningState buildPartitioning(List<String> answerSet, SimpleGraph<Node, Border> G, SimpleGraph<Partition,PartitionBorder> C) 
	{
		int numPartitions = GraphUtil.sizeOf(C);
		Map<Integer,Node> map = buildMapByValue(G);
		//Arrays.sort(nodes);		
		GraphPartitioningState Q =  new GraphPartitioningState();
		Partition[] pars = new Partition[numPartitions];
		for(int i =0 ; i < pars.length;i++)
		{
			pars[i] = new Partition(i+1);
 		}
		System.out.println(answerSet);
		ListIterator<String> it = answerSet.listIterator();
		int num = 0;
		while(it.hasNext())
		{
			String ans = it.next();
			if(ans.startsWith("belongs"))
			{
				int in = ans.indexOf(",");
				int left = ans.indexOf("(");
				int right = ans.indexOf(")");
				int nodeIndex = Integer.parseInt(ans.substring(left+1,in));
				int parNumber = Integer.parseInt(ans.substring(in+1,right));
				if(parNumber == 0)
				{
					Q.addToRemoved(map.get(nodeIndex));
				}
				else
				{
					pars[parNumber-1].addMember(map.get(nodeIndex));
				}
				num += 1;
			}
		}
		for(int i =0 ; i < pars.length;i++)
		{
			Q.addVertex(pars[i]);
		}
		GraphUtil.buildQuotientGraph(Q, G);
		return Q;
	}

	static Map<Integer,Node> buildMapByValue(SimpleGraph<Node,Border> G)
	{
		Map<Integer,Node> map = new HashMap<>();
		Node[] nodes = GraphUtil.getNodes(G);
		for(int i =0; i < nodes.length;i++)
		{
			map.put(nodes[i].getValue(),nodes[i]);
		}
		return map;
		
	}
	public GraphPartitioningState partition() 
	{
		List<String> answerSet = this.getAnswerSet();
		if(answerSet.get(0).equals("UNKNOWN") || answerSet.get(0).equals("UNSATISFIABLE") )
		{
			System.out.println("No solution was found or time limit reached before a solution is found.");
			return null;
		}
		return buildPartitioning(answerSet,G,C);
	}
	

	
}
