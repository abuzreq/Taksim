package asp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
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
public class ASPTestGrid 
{
	static int sizeOfBasicGraph = 70;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(1234);
	static int timeLimit = 10;// 0 for no limit, allow enough time for at least one solution to be found, the extra time is to limit optimization
	static int numModels = 0;  
	
	static boolean drawText = true;
	

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
		SimpleGraph<Node,Border> G = generator.generate(6,rand);
		
		//Generating the constraint graph
		final GraphPartitioningState C  =GraphUtil.generateChainGraph(3);getC();//getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
		
		
		// Coarsening
		if(afterCoarseningSize != -1)
		{
			G = GraphUtil.partitionToNodeGraph(GraphUtil.partition(G, afterCoarseningSize, PartitioningType.KERNEL_DETERMINISTIC,rand,true));
		}
				
		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		System.out.println("# of Pars = "+GraphUtil.sizeOf(C));
	

		//Mapping constraint, transform G, then pass the node as a constraint
		/*
		Node[] nodes = GraphUtil.getNodes(G);
		ArrayList<Node> start = new ArrayList<>();
		start.add(nodes[0]);
		start.add(nodes[1]);
		start.add(nodes[2]);
		
		Object[] objs = groupNodes(G,start);
		G = (SimpleGraph<Node, Border>) objs[0];
		Node startNode = (Node) objs[1];
		*/
		
		//Node Membership constraint
		
		Map<Integer,Integer> node2par = new HashMap<>();
		//node2par.put(startNode.getValue(), 1);
		
		
		//Size Optimization
		
		int numPartitions = GraphUtil.sizeOf(C);
		int[] pars = new int[0];
		//pars = range(1,numPartitions);
		OptType[] optTypesMax = getOptTypes(numPartitions,OptType.MAX);
		OptType[] optTypesMin = getOptTypes(numPartitions,OptType.MIN);
		int[] priorities = constant(numPartitions,1);
		
		Map<Integer,SimpleGraph<Node, Border>> par2graph = new HashMap<Integer, SimpleGraph<Node,Border>>();
		
		SimpleGraph<Node, Border> g1 = new SimpleGraph<Node, Border>(Border.class);
		
		Border b = new Border(new Node(1),new Node(2));
		g1.addVertex(b.getN1());
		g1.addVertex(b.getN2());
		g1.addEdge(b.getN1(), b.getN2(),b);
		
		Border b2 = new Border(b.getN2(),new Node(3));
		g1.addVertex(b2.getN1());
		g1.addVertex(b2.getN2());
		g1.addEdge(b2.getN1(), b2.getN2(),b2);
		
		SimpleGraph<Node, Border> g2 = new SimpleGraph<Node, Border>(Border.class);
		Border b3 = new Border(new Node(4),new Node(5));
		g2.addVertex(b3.getN1());
		g2.addVertex(b3.getN2());
		g2.addEdge(b3.getN1(), b3.getN2(),b3);
		
		par2graph.put(1, g1);
		par2graph.put(2, g2);
		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,timeLimit,numModels,node2par, pars,optTypesMax,priorities,par2graph);
		GraphPartitioningState result = null;
		int n =0 ;
		//while(result == null)
		{
		 result  = asp.partition();
		 n++;
		 //if(n > 50)
		 //	 break;
		}
		System.out.println(GraphUtil.getPartitions(result)[0].getMembers());
		generator.startDrawing(G,drawText);		
		TestsUtil.colorizeRandom(result,Color.WHITE);
	}

	static OptType[] getOptTypes(int n,OptType type)
	{
		OptType[] arr = new OptType[n];
		for(int i = 0;i < arr.length;i++)
		{
			arr[i] = type;
		}
		return arr;
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
