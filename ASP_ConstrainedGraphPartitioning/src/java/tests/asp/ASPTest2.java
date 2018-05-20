package asp;

import java.awt.Color;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.graph.SimpleGraph;

import examples.GridGenerator;
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
	static int timeLimit = 0;// 0 for no limit, allow enough time for at least one solution to be found, the extra time is to limit optimization
	static int numModels = 1;  
	
	static boolean drawText = true;
	
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_adjacency_constraints = System.getProperty("user.dir") + "/src/java/tests/files/adjacency_constraints.lp";
	private final static String rulefile_node_constraints = System.getProperty("user.dir") + "/src/java/tests/files/node_constraints.lp";
	private final static String rulefile_partitions_size_opt = System.getProperty("user.dir") + "/src/java/tests/files/size_opt.lp";
	private static EdgeFactory<Partition,PartitionBorder> bf =  new EdgeFactory<Partition,PartitionBorder>()
	{

		@Override
		public PartitionBorder createEdge(Partition sourceVertex, Partition targetVertex)
		{
			PartitionBorder b = new PartitionBorder(sourceVertex,targetVertex);
			return b;
		}
		
	};
	private static VertexFactory<Partition> vf = new VertexFactory<Partition>() {
		int n = 0;
		@Override
		public Partition createVertex() {
			Partition par = new Partition(n);
			n++;
			return par;
		}};
	public static void main(String[] args) 
	{
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(50);
		SimpleGraph<Node,Border> G = generator.generate(7,rand);
		
		//Generating the constrain graph
		//final GraphPartitioningState C  = GraphUtil.generateChainGraph(3);
		String filePath = "/home/abuzreq/eclipse-workspace/ASP-All/ASP_ConstrainedGraphPartitioning/src/java/tests/test_graphs/tree3.in"; 
		//final GraphPartitioningState C  = getC();//TestsUtil.readConstraintGraphs(filePath).get(0);// getC();
		SimpleGraph<Partition,PartitionBorder> Cc  = new SimpleGraph<Partition,PartitionBorder>(bf);//GraphUtil.generateChainGraph(8);
		GnmRandomGraphGenerator<Partition,PartitionBorder> gen = new GnmRandomGraphGenerator<>(4, 3);
		gen.generateGraph(Cc, vf, null);
		System.out.println(Cc);
		GraphPartitioningState C = new GraphPartitioningState();
		Partition[] pars = GraphUtil.getPartitions(Cc);
		for(int i =0 ; i <  pars.length;i++ )
		{
			C.addVertex(pars[i]);
		}
		for(PartitionBorder pb: Cc.edgeSet())
		{
			//PartitionBorder b = new PartitionBorder(pars[pb.getP1().getNumber()-1], pars[pb.getP2().getNumber()-1]);
			//C.addEdge(pars[pb.getP1().getNumber()-1],pars[pb.getP2().getNumber()-1],b);
			C.addEdge(pb.getP1(), pb.getP2(),pb);
		}
		System.out.println(C);
		C = GraphUtil.generateChainGraph(10);//getC();//
		if(afterCoarseningSize != -1)
		{
			G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true));
			//G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize/2, PartitioningType.KMEANS_STOCHASTIC,rand,true));
		}
		
		int numPartitions = GraphUtil.sizeOf(C);
		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		System.out.println("# of Pars = "+numPartitions);
		
		Node[] nodes = GraphUtil.getNodes(G);
		ArrayList<Node> start = new ArrayList<>();
		start.add(nodes[0]);
		start.add(nodes[1]);

		
		//Object[] objs = groupNodes(G,start);
		//G = (SimpleGraph<Node, Border>) objs[0];
		//Node startNode = (Node) objs[1];
		
		Map<Integer,Integer> node2par = new HashMap<>();
		//node2par.put(startNode.getValue(), 1);
		//node2par.put(1, 1);
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
		//node(0..n-1).
		//Map<Integer,Integer[]> par2nodes = new HashMap<>();
		//par2nodes.put(1, new Integer[] {1,2});
		System.out.println(G);
		Border[] bors = GraphUtil.getBorders(G);
		for(Border b : bors)
		{
			//System.out.println(b.getN1().getValue() + " "+b.getN2().getValue());
		}
		

		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_adjacency_constraints);
		writeNodeInPartitionConstraintsToFile(node2par,rulefile_node_constraints);
		
		OptType[] optTypes = {OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX,OptType.MAX};
		OptType[] optTypes2 = {OptType.MIN,OptType.MIN,OptType.MIN,OptType.MIN,OptType.MIN,OptType.MIN,OptType.MIN,OptType.MIN};
		//writePartitionsSizeOptimizationToFile(range(1,numPartitions),optTypes,constant(numPartitions,1),rulefile_partitions_size_opt);
		
		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,timeLimit,numModels,
				rulefile_edges, rulefile_adjacency_constraints,rulefile_node_constraints,rulefile_partitions_size_opt);

		GraphPartitioningState result  = asp.partition();


		generator.startDrawing(G,drawText);		
		TestsUtil.colorizeRandom(result,Color.WHITE);
	}


	static int formNumber(ArrayList<Node> nodes)
	{
		Comparator<Node> comp = new Comparator<Node>() {
			
			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				return o2.getValue() - o1.getValue();
			}
		};
		Collections.sort(nodes, comp);
		return nodes.get(0).getValue();
		/*
		for(int i =0 ; i < nodes.size();i++)
		{
			str += nodes.get(i).getValue();
		}
		return Integer.parseInt(str+"00");
		*/
	}
	private static Object[] groupNodes(SimpleGraph<Node,Border> G, ArrayList<Node> nodes)
	{
		int num = formNumber(nodes);
		
		Node parent = new Node(num);
		parent.setCluster(nodes);
		List<Border> edges = new LinkedList<Border>();
		for(int i = 0 ; i < nodes.size();i++)
		{
			Set<Border> eds = GraphUtil.getEdgesOf(G, nodes.get(i));
			for(Border e : eds)
			{
				boolean n1 = nodes.contains(e.getN1());
				boolean n2 = nodes.contains(e.getN2());
				if(n1 && n2)
					continue;
				Border b = new Border(n1?e.getN2():parent,n2?e.getN1():parent);
				edges.add(b);
			}			
		}
		G.removeAllVertices(nodes);
		G.addVertex(parent);
		for(int i = 0 ; i < edges.size();i++)
		{
			Border e = edges.get(i);
			G.addEdge(e.getN1(), e.getN2(),e);
		}
		return new Object[] {G,parent};
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
	private static int[] constant(int n,int constant)
	{
		int[] arr = new int[n];
		for(int i = 0 ; i < arr.length;i++)
		{
			arr[i] = constant;
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
	private static void writePartitionsSizeOptimizationToFile(int[] pars,OptType[] opt,int[] priorities, String rulefile) 
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
