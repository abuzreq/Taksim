package asp;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

import asp4j.program.Program;
import asp4j.program.ProgramBuilder;
import asp4j.solver.SolverBase;
import asp4j.solver.SolverClingo;
import asp4j.solver.SolverException;
import asp4j.solver.object.Binding;
import asp4j.solver.object.Filter;
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
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_adjacency_constraints = System.getProperty("user.dir") + "/src/java/tests/files/adjacency_constraints.lp";
	private final static String rulefile_node_constraints = System.getProperty("user.dir") + "/src/java/tests/files/node_constraints.lp";
	private final static String rulefile_partitions_size_opt = System.getProperty("user.dir") + "/src/java/tests/files/size_opt.lp";

	private String[] extraRuleFiles;
	private boolean allowNodeRemoval;
	private int timeLimit,numModels;

	private SimpleGraph<Node, Border> G;
	private GraphPartitioningState C;
	
	private Random rand ;
	public ASPConstrainedGraphPartitioning(SimpleGraph<Node, Border> G, GraphPartitioningState C, boolean allowNodeRemoval, int timeLimit, int numModels, Map<Integer, Integer> node2par,int[] pars,OptType[] types,int[] priorities)
	{
		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_adjacency_constraints);
		writeNodeInPartitionConstraintsToFile(node2par,rulefile_node_constraints);		
		writePartitionsSizeOptimizationToFile(pars,types,priorities,rulefile_partitions_size_opt);
		

		this.extraRuleFiles = new String[]{rulefile_edges,rulefile_adjacency_constraints,rulefile_node_constraints,rulefile_partitions_size_opt};
		this.G = G;
		this.C = C;
		
		this.numNodes = GraphUtil.sizeOf(G);
		this.numPartitions = GraphUtil.sizeOf(C);
		
		this.allowNodeRemoval = allowNodeRemoval;
		this.timeLimit = timeLimit;
		this.numModels = numModels;
		
		this.rand = new Random();
		
			
	}
	
	public ASPConstrainedGraphPartitioning(SimpleGraph<Node, Border> G, GraphPartitioningState C, boolean allowNodeRemoval, int timeLimit, int numModels, Map<Integer, Integer> node2par)
	{
		this(G, C, allowNodeRemoval, timeLimit, numModels, node2par,new int[0],new OptType[0],new int[0]);
	}
	public ASPConstrainedGraphPartitioning(SimpleGraph<Node, Border> G, GraphPartitioningState C, boolean allowNodeRemoval, int timeLimit, int numModels)
	{
		this(G, C, allowNodeRemoval, timeLimit, numModels, new HashMap<Integer, Integer>(),new int[0],new OptType[0],new int[0]);
	}
	public ASPConstrainedGraphPartitioning(SimpleGraph<Node, Border> G, GraphPartitioningState C, boolean allowNodeRemoval, int timeLimit, int numModels,int[] pars,OptType[] types,int[] priorities)
	{
		this(G, C, allowNodeRemoval, timeLimit, numModels, new HashMap<Integer, Integer>(),pars,types,priorities);
	}
	private List<String> getAnswerSet(int seed)
	{
		try {
			return getAnswerSet(new SolverClingo(numModels), seed, rulefile_main,extraRuleFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	private int numNodes ;
	private int numPartitions;
	private List<String> getAnswerSet(SolverBase externalSolver,int seed,String mainRuleFile, String[] extraRuleFiles) throws Exception 
	{
		externalSolver.setExtraParams(" --const n="+numNodes+" --const p="+numPartitions + " --rand-freq=0.0" + " --const r="+(allowNodeRemoval?0:1) + " --time-limit="+timeLimit + " --seed="+seed);// +" -t 4,split");//+
		ProgramBuilder<Object> pb = new ProgramBuilder<>();
		
		pb.add(new File(mainRuleFile));
		for(String rulefile : extraRuleFiles)
		{
			pb.add(new File(rulefile));
		}
			
		Program<Object> program = pb.build();

		List<String> answerSets = externalSolver.getAnswerSetsAsStrings(program);
		
		return answerSets;	
	}

	 
	private static void writeBasicGraphToFile(SimpleGraph<Node, Border> G, String rulefile) 
	{
		Border[] edges = GraphUtil.getBorders(G);
		Node[] nodes = GraphUtil.getNodes(G);
		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			
			sb.append("node(");
			for (int i = 0; i < nodes.length; i++) 
			{
				
				sb.append(nodes[i].getValue());
				if(i < nodes.length-1)
					sb.append(";");				
			}
			sb.append(").\n");
			
			for (int i = 0; i < edges.length; i++) 
			{
				sb.append("edge(");
				sb.append(edges[i].getN1().getValue());
				sb.append(",");
				sb.append(edges[i].getN2().getValue());
				sb.append(").\n");
			}
			fw.write(sb.toString());
			fw.close();		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/*
	:-not edge_q(1,2).
	:-not edge_q(1,3).
	:-not edge_q(2,4).
	:-not edge_q(3,4).
	:-edge_q(1,4).
	:-edge_q(2,3).
	 * */
	private static void writeConstraintGraphToFile(GraphPartitioningState C, String rulefile) 
	{
		ComplementGraphGenerator<Partition,PartitionBorder> ccg =  new ComplementGraphGenerator(C);
		SimpleGraph<Partition,PartitionBorder> complemntC = new SimpleGraph<Partition,PartitionBorder>(PartitionBorder.class);
		ccg.generateGraph(complemntC, new PartitionBorderFactory());
		
		PartitionBorder[] edges = GraphUtil.getPartitionsBorders(C);
		PartitionBorder[] complementEdges = GraphUtil.getPartitionsBorders(complemntC);

		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < edges.length; i++) 
			{
				int p1 = edges[i].getP1().getNumber()+1;
				int p2 = edges[i].getP2().getNumber()+1;
				
				sb.append(":- not edge_q(");
				sb.append(Math.min(p1,p2));
				sb.append(",");
				sb.append(Math.max(p1,p2));
				sb.append(") ; not edge_q(");
				sb.append(Math.max(p1,p2));
				sb.append(",");
				sb.append(Math.min(p1,p2));
				sb.append(").\n");
			}
			for (int i = 0; i < complementEdges.length; i++) 
			{
				int p1 = complementEdges[i].getP1().getNumber()+1;
				int p2 = complementEdges[i].getP2().getNumber()+1;
				
				sb.append(":- edge_q(");
				sb.append(Math.min(p1,p2));
				sb.append(",");
				sb.append(Math.max(p1,p2));
				sb.append(").\n");
				
				sb.append(":- edge_q(");
				sb.append(Math.max(p1,p2));
				sb.append(",");
				sb.append(Math.min(p1,p2));
				sb.append(").\n");
			}
			fw.write(sb.toString());
			fw.close();		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	private static void writeNodeInPartitionConstraintsToFile(Map<Integer, Integer> node2par,String rulefile) 
	{
		//:- belongs(1,1).
		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			for (Integer node : node2par.keySet()) 
			{
				int par = node2par.get(node);
				
				sb.append(":- not belongs(");
				sb.append(node);
				sb.append(",");
				sb.append(par);
				sb.append(").\n");
			}
			fw.write(sb.toString());
			fw.close();		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	private static class PartitionBorderFactory implements EdgeFactory<Partition,PartitionBorder>
	{

		@Override
		public PartitionBorder createEdge(Partition sourceVertex, Partition targetVertex)
		{
			PartitionBorder pb = new PartitionBorder(sourceVertex,targetVertex);
			return pb;
		}
		
	}
	
	
	/**
	 * 
	 * @param pars
	 * @param opt an array of same length as pars, MAX for maximization, MAX for minimization
	 * @param priorities the higher the more priority is given, priority numbers are relative to optimization being done
	 *  (i.e. a priority of 5 for a maximization statemnt is only higher than other maximization statments)
	 * @param rulefile
	 */
	private static void writePartitionsSizeOptimizationToFile(int[] pars,OptType[] opt,int[] priorities, String rulefile) 
	{
		//#maximize {N@priority_i: belongs(N,P),node(N),P==i}. 
		//%#maximize {T:count(P,T),partition(P),P==1}.

		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < pars.length;i++)
			{
				if(opt[i] == OptType.MAX)
					sb.append("#maximize {T@");
				else if (opt[i]==OptType.MIN)
					sb.append("#minimize {N@");

				sb.append(priorities[i]);
				sb.append(": count(P,T),partition(P),P==");
				sb.append(pars[i]);
				sb.append("}.\n");
			}
			fw.write(sb.toString());
			fw.close();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
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
		//System.out.println(answerSet);
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
	public GraphPartitioningState partition(int seed) 
	{
		List<String> answerSet = this.getAnswerSet(seed);
		if(answerSet.get(0).equals("UNKNOWN") || answerSet.get(0).equals("UNSATISFIABLE") )
		{
			//System.out.println("No solution was found or time limit reached before a solution is found.");
			return null;
		}
		//System.out.println(answerSet);
		return buildPartitioning(answerSet,G,C);
	}
	public GraphPartitioningState partition() 
	{
		return partition(rand.nextInt(Integer.MAX_VALUE));
	}

	
}
