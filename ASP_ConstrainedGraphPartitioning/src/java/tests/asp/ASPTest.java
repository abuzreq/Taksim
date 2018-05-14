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

import examples.VoronoiGenerator;
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.enums.PartitioningType;
import util.GraphUtil;
import util.TestsUtil;
public class ASPTest 
{
	static int sizeOfBasicGraph = 120;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(1234);
	static int timeLimit = 360;// 0 for no limit, allow enough time for atleast one solution to be found, the extra time is to limit optimization
	static int numModels = 1;  //0 for finding the optimal model
	
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_constraints = System.getProperty("user.dir") + "/src/java/tests/files/adjacency_constraints.lp";
	private final static String rulefile_node_constraints = System.getProperty("user.dir") + "/src/java/tests/files/node_constraints.lp";
	


	public static void main(String[] args) 
	{
		System.out.println(GraphUtil.generateBasicGridGraph(5));
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(8);
		String filePath = ""; 
		//final GraphPartitioningState C  = getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
		
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, true);
		
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
		
		if(afterCoarseningSize != -1)
			G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true));
		//G = TestsUtil.readBasicGraphs("src/java/tests/test_graphs/grid5x5.in").get(0);
		
		Map<Integer,Integer> node2par = new HashMap<>();
		/*
		node2par.put(1, 1);
		node2par.put(2, 1);
		node2par.put(13, 1);
		node2par.put(0, 2);
		node2par.put(19, 2);
		*/
		node2par.put(1, 1);
		node2par.put(2, 1);
		node2par.put(13, 1);
		node2par.put(12, 1);
		
		node2par.put(0, 2);
		node2par.put(5, 2);
		
		node2par.put(8, 2);
		
		node2par.put(14, 4);
		node2par.put(16, 4);
		node2par.put(30, 5);
		node2par.put(50, 7);
		node2par.put(70,8);
		
		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_constraints);
		writeNodeInPartitionConstraintsToFile(node2par,rulefile_node_constraints);

		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(GraphUtil.sizeOf(G),GraphUtil.sizeOf(C),
				allowNodeRemoval,timeLimit,numModels,rulefile_edges, rulefile_constraints,rulefile_node_constraints);
		List<String> answerSet = asp.getAnswerSet();
		if(answerSet.get(0).equals("UNKNOWN") || answerSet.get(0).equals("UNSATISFIABLE") )
		{
			System.out.println("No solution was found or time limit reached before a solution is found.");
			System.exit(0);
		}
		GraphPartitioningState result = buildPartitioning(answerSet,G,C);
		System.out.println(result);
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
	private static void writeNodeInPartitionConstraintsToFile(Map<Integer, Integer> node2par,String rulefile) 
	{
		//:- not belongs(1,1).
		try 
		{	
			FileWriter fw = new FileWriter(rulefile);
			StringBuilder sb = new StringBuilder();
			
			int n = node2par.keySet().size();
			for (Integer node : node2par.keySet()) 
			{
				n--;
				
				int par = node2par.get(node);
				sb.append(":- ");
				sb.append(" not belongs(");
				sb.append(node);
				sb.append(",");
				sb.append(par);
				if(n > 0)
				{
					sb.append(").\n");
				}
				else
					sb.append(").\n");
			}
			//sb.append(".\n");
			fw.write(sb.toString());
			fw.close();		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
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
