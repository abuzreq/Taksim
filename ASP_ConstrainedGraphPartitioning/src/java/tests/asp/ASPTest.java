package asp;

import java.awt.Color;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
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
	static int sizeOfBasicGraph = 1000;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 70;
	static Random rand =  new Random(1234);
	
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_constraints = System.getProperty("user.dir") + "/src/java/tests/files/constraints.lp";
	public static void main(String[] args) 
	{
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(10);
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
		G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true));

		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_constraints);
		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(rulefile_edges, rulefile_constraints,GraphUtil.sizeOf(G),GraphUtil.sizeOf(C));
		List<String> answerSet = asp.getAnswerSet();
		GraphPartitioningState result = buildPartitioning(answerSet,G,C);
		//Create P lists (including removed par),go through Belongs or Contains objects and add to one of those lists accordingly
		//create a GraphPartitioningState and pass it to
		System.out.println(result);
		TestsUtil.colorizeRandom(result,Color.WHITE);
	}

	private static GraphPartitioningState buildPartitioning(List<String> answerSet, SimpleGraph<Node, Border> G, SimpleGraph<Partition,PartitionBorder> C) 
	{
		int numPartitions = GraphUtil.sizeOf(C);
		Node[] nodes = GraphUtil.getNodes(G);
		Arrays.sort(nodes);
		System.out.println(Arrays.deepToString(nodes));
		
		GraphPartitioningState Q =  new GraphPartitioningState();
		Partition[] pars = new Partition[numPartitions];
		for(int i =0 ; i < pars.length;i++)
		{
			pars[i] = new Partition(i+1);
 		}
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
	
	
}
