package asp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jgraph.JGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.json.JSONArray;
import org.json.JSONObject;

import asp.ASPConstrainedGraphPartitioning.Pair;
import examples.GridGenerator;
import examples.GridGenerator.CellNode;
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
	static Random rand =  new Random(12345);
	static int timeLimit = 0;// 0 for no limit, allow enough time for at least one solution to be found, the extra time is to limit optimization
	static int numModels = 1;  
	
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
		
	static private int dim = 12;		
	public static void main(String[] args) 
	{
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(100);
		SimpleGraph<Node,Border> G = generator.generate(dim,rand);
		//6, 21 nodes
		//Generating the constraint graph
		//final GraphPartitioningState C  =GraphUtil.generateChainGraph(5);//getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
		final GraphPartitioningState C = TestsUtil.readConstraintGraphs("G:\\GitHub\\ASP_ConstrainedGraphPartitioning\\ASP_ConstrainedGraphPartitioning\\src\\java\\tests\\test_graphs\\tsmith.in").get(0);
		System.out.println(C.getNamesMap());
		/*
		Map<Integer,String> missionsNames = new HashMap<Integer,String>();
		missionsNames.put(1, "start");
		missionsNames.put(2, "fight");
		missionsNames.put(3, "trap");
		missionsNames.put(4, "loot");
		missionsNames.put(5, "fight");
		missionsNames.put(6, "puzzle");
		missionsNames.put(7, "boss");
		missionsNames.put(8, "end");
		*/
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
		/*
		Map<Integer,Integer> node2par = new HashMap<>();
		node2par.put(startNode.getValue(), 1);
		*/
		
		//Size Optimization
		
		int numPartitions = GraphUtil.sizeOf(C);
		int[] pars = range(1,numPartitions);
		OptType[] optTypesMax = getOptTypes(numPartitions,OptType.MAX);
		OptType[] optTypesMin = getOptTypes(numPartitions,OptType.MIN);
		int[] priorities = constant(numPartitions,1);
		
		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,timeLimit,numModels);//,pars,optTypesMax,priorities);
		Pair pair = null;
		int n =0 ;
		while(pair == null)
		{
		 pair  = asp.partitionFull(12345);
		 n++;
		 if(n > 50)
			 break;
		}
		GraphPartitioningState result = pair.state;
		System.out.println(GraphUtil.getPartitions(result)[0].getMembers());
		generator.startDrawing(G,drawText);		
		TestsUtil.colorizeRandom(result,Color.WHITE);
		
		String bordersJson = getBordersJSON(pair.answerSet);
		String basicGraphJson = toJSON(G,dim,dim);	
		String constraintGraphJson = toJSON(C,C.getNamesMap());
		String partitionsJson =  getPartitionsJSON(result,C.getNamesMap());
		
		System.out.println(basicGraphJson);
		System.out.println(constraintGraphJson);
		System.out.println(bordersJson);
		System.out.println(partitionsJson);
		
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
	
	static String getPartitionsJSON(GraphPartitioningState state,Map<Integer,String> namesMap)
	{
		int numPartitions = GraphUtil.sizeOf(state);
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		
		{//Handling removed 
			JSONObject removedPartitionObject = new JSONObject();
			removedPartitionObject.put("id", 0);
			JSONArray nodes = new JSONArray();
			for(Node v : state.getRemoved())
			{		
				nodes.put(v.getValue());
			}
			removedPartitionObject.put("nodes", nodes);		
			removedPartitionObject.put("adjacentPartitions", new JSONArray());
			arr.put(removedPartitionObject);
		}
		System.out.println(namesMap);
		Partition[] partitions = GraphUtil.getPartitions(state);
		for(int i = 0;i < numPartitions;i++)
		{
			JSONObject partitionObject = new JSONObject();
			partitionObject.put("id", partitions[i].getNumber());// Partitions from asp approach start from 1 normally
			System.out.println(partitions[i].getNumber()+ " "+namesMap.get(partitions[i].getNumber()-1));
			partitionObject.put("type",namesMap.get(partitions[i].getNumber()-1));

			JSONArray nodes = new JSONArray();
			for(Node v : partitions[i].getMembers())//TODO handling coarsening
			{		
				nodes.put(v.getValue());
			}
			partitionObject.put("nodes", nodes);
			
			Set<Integer> adjacentPartitions =  new HashSet<Integer>();
			JSONArray neighbors = new JSONArray();
			HashMap<Integer, List<Node>> map = new HashMap<>();
			for(Node v : partitions[i].getNeighbors())
			{		
				if(!map.containsKey(v.getContainer().getNumber()))
				{
					map.put(v.getContainer().getNumber(), new LinkedList<Node>());
				}
				map.get(v.getContainer()).add(v);
			}
			for(int p : map.keySet())
			{
				JSONObject neighborObject = new JSONObject();
				neighborObject.put("partition", p);
				JSONArray neighborNodes = new JSONArray();
				for(Node n : map.get(p))
				{
					neighborNodes.put(n.getValue());
				}
				neighborObject.put("nodes", neighborNodes);
				neighbors.put(neighborObject);
			}
			//System.out.println(neighbors);

			partitionObject.put("neighborNodes", neighbors);
			partitionObject.put("adjacentPartitions",map.keySet().toArray());
			
			
			arr.put(partitionObject);
		}
			
		obj.put("partitions",arr);
		return obj.toString();
	}
	private static String toJSON(SimpleGraph<Node, Border> G, int w, int h)
	{
		JSONObject obj = new JSONObject();
		JSONObject graph = new JSONObject();
		
		JSONArray nodes = new JSONArray();
		for(Node v : G.vertexSet())
		{
			JSONObject node = new JSONObject();
			node.put("id", v.getValue());
			//TODO generalize for Voronoi as well
			//TODO handle coursening
			Partition par = v.getContainer();
			node.put("partition", par == null?0:par.getNumber());//So partitions numbers start from 1, and 0 be for removed par		
			//CellNode cellNode = (CellNode)v;			
			//node.put("x", cellNode.cell.getX());
			//node.put("y", cellNode.cell.getY());
			node.put("category", "");
			JSONArray edges = new JSONArray();		
			for(Border e : G.edgesOf(v))
			{
				Node n = e.getN1().getValue() == v.getValue()?e.getN2():e.getN1();
				edges.put(n.getValue());
			}
			node.put("edges", edges);
			nodes.put(node);
		}
		graph.put("nodes", nodes);
		/*
		JSONArray edges = new JSONArray();
	
		for(Border e : G.edgeSet())
		{
			JSONObject edge = new JSONObject();
			int min = Math.min(e.getN1().getValue(),e.getN2().getValue());
			int max = Math.max(e.getN1().getValue(),e.getN2().getValue());
			edge.put("source", min);
			edge.put("target", max);
			edges.put(edge);		
		}
		graph.put("edges", edges);
		*/
		obj.put("graph", graph);
		obj.put("graphSize", h*w);
		obj.put("graphWidth", w);
		obj.put("graphHeight", h);
		return obj.toString();
	}
	private static String toJSON(GraphPartitioningState C, Map<Integer,String> types)
	{
		JSONObject obj = new JSONObject();
		JSONObject graph = new JSONObject();
		
		JSONArray nodes = new JSONArray();
		for(Partition v : C.vertexSet())
		{
			JSONObject node = new JSONObject();
			node.put("id", v.getNumber() + 1);//So partitions numbers start from 1
			node.put("type", types.get(v.getNumber()+1));
			Partition[] adjacents = GraphUtil.adjacentsOf(C, v);
			JSONArray arr = new JSONArray();
			for(int i = 0;i < adjacents.length;i++)
			{
				//TODO A temporary hack (assumes if mission X happens after Y, its number is larger
				//. Instead, C should be a directed graph and I should pass the next only
				if(adjacents[i].getNumber() > v.getNumber())
					arr.put(adjacents[i].getNumber()+1);
			}
			node.put("nextObjs",arr );
			//TODO add other properties if any
			nodes.put(node);
		}
		graph.put("objectives", nodes);
		/*
		JSONArray edges = new JSONArray();
		for(PartitionBorder e : C.edgeSet())
		{
			JSONObject edge = new JSONObject();
			int min = Math.min(e.getP1().getNumber(),e.getP2().getNumber()) + 1 ;//So partitions numbers start from 1
			int max = Math.max(e.getP1().getNumber(),e.getP2().getNumber()) + 1 ;
			edge.put("source", min);
			edge.put("target", max);
			edges.put(edge);		
		}
		graph.put("edges", edges);*/
		
		obj.put("graph", graph);
		return obj.toString();
	}
	private static String getBordersJSON(List<String> answerSet)
	{
		Map<PartitionBorder,List<Border>> bordersMap = new HashMap<>();
		Iterator<String> it = answerSet.iterator();
		while(it.hasNext())
		{
			String ans = it.next();
			if(ans.startsWith("borders"))
			{
				int left = ans.indexOf("(");
				int right = ans.indexOf(")");
				String arguments = ans.substring(left+1,right);
				String[] tokens = arguments.split(",");
				int[] nums = new int[tokens.length];
				for(int i = 0; i< nums.length;i++)
				{
					nums[i] = Integer.parseInt(tokens[i]);
				}
				int min = Math.min(nums[0],nums[1]);
				int max = Math.max(nums[0],nums[1]);
				PartitionBorder pb =  new PartitionBorder(new Partition(min), new Partition(max));
				if(!bordersMap.containsKey(pb))
				{
					bordersMap.put(pb, new LinkedList<Border>());
				}
				//TODO handle the case when nodes are clusters,
				//TODO verify that the node belong to par 1 comes first etc
				if(min == nums[0])
					bordersMap.get(pb).add(new Border(new Node(nums[2]),new Node(nums[3])));
				else 
					bordersMap.get(pb).add(new Border(new Node(nums[3]),new Node(nums[2])));
			}
			
		}
		JSONArray bordersJson = new JSONArray();
		for(PartitionBorder pb: bordersMap.keySet())
		{
			JSONObject partitionsPair = new JSONObject();
			partitionsPair.put("source", pb.getP1().getNumber());
			partitionsPair.put("target", pb.getP2().getNumber());
	
			JSONArray tuples = new JSONArray();
			for(Border b : bordersMap.get(pb))
			{
				JSONObject tuple = new JSONObject();
				tuple.put("nodeSource",b.getN1().getValue());
				tuple.put("nodeTarget",b.getN2().getValue());
				tuples.put(tuple);
			}
			partitionsPair.put("doors", tuples);	
	
			JSONObject pairObj = new JSONObject();
			pairObj.put("pair", partitionsPair);
			bordersJson.put(partitionsPair);
		}
		
		return bordersJson.toString();
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
	public static GraphPartitioningState getC2()
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
