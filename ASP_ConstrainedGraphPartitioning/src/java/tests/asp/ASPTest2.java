package asp;

import java.awt.Color;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleGraph;

import examples.GridGenerator;
import examples.GridGeneratorDrawer;
import examples.VoronoiGenerator;
import processing.core.PApplet;
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.enums.PartitioningType;
import util.GraphUtil;
import util.TestsUtil;
public class ASPTest2 
{
	static int sizeOfBasicGraph = 70;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(1234);
	static int timeLimit = 60;// 0 for no limit, allow enough time for at least one solution to be found, the extra time is to limit optimization
	static int numModels = 1;  
	
	static boolean drawText = true;
	
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_adjacency_constraints = System.getProperty("user.dir") + "/src/java/tests/files/adjacency_constraints.lp";
	private final static String rulefile_node_constraints = System.getProperty("user.dir") + "/src/java/tests/files/node_constraints.lp";
	private final static String rulefile_partitions_size_max = System.getProperty("user.dir") + "/src/java/tests/files/size_max.lp";

	public static void main(String[] args) 
	{
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(50);
		SimpleGraph<Node,Border> G = generator.generate(8,rand);

		
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(4);
		String filePath = ""; 
		//final GraphPartitioningState C  = getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
				
		
		if(afterCoarseningSize != -1)
			G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true));
		
		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		
		
		Map<Integer,Integer> node2par = new HashMap<>();
		node2par.put(0, 1);
		node2par.put(1, 1);
		/*
		node2par.put(2, 1);
		node2par.put(3, 1);
		node2par.put(4, 1);
		node2par.put(5, 1);
		node2par.put(6, 1);
		node2par.put(7, 1);
		node2par.put(8, 1);
		node2par.put(9, 1);
		node2par.put(10, 1);
		*/
		
		Map<Integer,Integer[]> par2nodes = new HashMap<>();
		par2nodes.put(1, new Integer[] {1,2});

		
		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_adjacency_constraints);
		writeNodeInPartitionConstraintsToFile(node2par,rulefile_node_constraints);
		
		OptType[] optTypes = {OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX};
		writePartitionsMaximizationsToFile(range(1,4),optTypes,ones(4),rulefile_partitions_size_max);
		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(GraphUtil.sizeOf(G),GraphUtil.sizeOf(C),
				allowNodeRemoval,timeLimit,numModels,rulefile_edges, rulefile_adjacency_constraints,rulefile_node_constraints,rulefile_partitions_size_max);
		List<String> answerSet = asp.getAnswerSet();
		if(answerSet.get(0).equals("UNKNOWN") || answerSet.get(0).equals("UNSATISFIABLE") )
		{
			System.out.println("No solution was found or time limit reached before a solution is found.");
			System.exit(0);
		}
		GraphPartitioningState result = buildPartitioning(answerSet,G,C);
		System.out.println(result);
		//PApplet.main("examples.GridGeneratorDrawer");
		System.out.println("1");

		generator.startDrawing(G,drawText);
		
		TestsUtil.colorizeRandom(result,Color.WHITE);
	}




	private static GraphPartitioningState buildPartitioning(List<String> answerSet, SimpleGraph<Node, Border> G, SimpleGraph<Partition,PartitionBorder> C) 
	{
		int numPartitions = GraphUtil.sizeOf(C);
		Node[] nodes = GraphUtil.getNodes(G);
		Arrays.sort(nodes);		
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
			if(ans.startsWith("contains"))
			{
				int in = ans.indexOf(",");
				int left = ans.indexOf("(");
				int right = ans.indexOf(")");
				int parNumber = Integer.parseInt(ans.substring(left+1,in));
				int nodeIndex = Integer.parseInt(ans.substring(in+1,right));
				if(parNumber == 0)
				{
					Q.addToRemoved(nodes[nodeIndex]);
				}
				else
				{
					pars[parNumber-1].addMember(nodes[nodeIndex]);
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
	private static int[] range(int l,int u)
	{
		int[] arr = new int[u-l+1];
		for(int i = 0 ; i < arr.length;i++)
		{
			arr[i] = l + i;
		}
		return arr;
	}
	private static int[] ones(int n)
	{
		int[] arr = new int[n];
		for(int i = 0 ; i < arr.length;i++)
		{
			arr[i] = 1;
		}
		return arr;
	}
	
	enum OptType
	{MAX,MIN}
	
	/**
	 * 
	 * @param pars
	 * @param opt an array of same length as pars, MAX for maximization, MAX for minimization
	 * @param priorities the higher the more priority is given, priority numbers are relative to optimization being done
	 *  (i.e. a priority of 5 for a maximization statemnt is only higher than other maximization statments)
	 * @param rulefile
	 */
	private static void writePartitionsMaximizationsToFile(int[] pars,OptType[] opt,int[] priorities, String rulefile) 
	{
		//#maximize {N@priority_i: belongs(N,P),node(N),P==i}. 

		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < pars.length;i++)
			{
				if(opt[i] == OptType.MAX)
					sb.append("#maximize {N@");
				else if (opt[i]==OptType.MIN)
					sb.append("#minimize {N@");

				sb.append(priorities[i]);
				sb.append(": belongs(N,P),node(N),P==");
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
	/*
	private static void writeNodeInPartitionConstraintsToFile(Map<Integer, Integer[]> par2nodes,String rulefile) 
	{
		//:- belongs(1,1).
		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			for (Integer par : par2nodes.keySet()) 
			{
				Integer[] nodes = par2nodes.get(par);
				
				sb.append(":- belongs(");
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
		
	}*/
	private static void writeBasicGraphToFile(SimpleGraph<Node, Border> G, String rulefile) 
	{
		Border[] edges = GraphUtil.getBorders(G);
		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
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
	private static class PartitionBorderFactory implements EdgeFactory<Partition,PartitionBorder>
	{

		@Override
		public PartitionBorder createEdge(Partition sourceVertex, Partition targetVertex)
		{
			PartitionBorder pb = new PartitionBorder(sourceVertex,targetVertex);
			return pb;
		}
		
	}
	public static GraphPartitioningState getC()
	{
		GraphPartitioningState C = new GraphPartitioningState();
		Partition p0 = new Partition(0);
		Partition p1 = new Partition(1);
		Partition p2 = new Partition(2);
		Partition p3 = new Partition(3);
		Partition p4 = new Partition(4);
		Partition p5 = new Partition(5);
		Partition p6 = new Partition(6);
		Partition p7 = new Partition(7);
		C.addVertex(p0);
		C.addVertex(p1);
		C.addVertex(p2);
		C.addVertex(p3);
		C.addVertex(p4);
		C.addVertex(p5);
		C.addVertex(p6);
		C.addVertex(p7);
		
		PartitionBorder[] edges =
			{		
				new PartitionBorder(p0, p1),
				new PartitionBorder(p1, p2),
				new PartitionBorder(p2, p3),
				new PartitionBorder(p3, p4),
				new PartitionBorder(p4, p5),
				new PartitionBorder(p1, p5),
				new PartitionBorder(p5, p6),
				new PartitionBorder(p6, p7)
			};
		for(int i = 0 ; i < edges.length;i++)
		{
			C.addEdge(edges[i].getP1(), edges[i].getP2(),edges[i]);
		}
		return C;
	}
	
}
