package asp;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

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
import search.basic.PartitionNodePair;
import search.enums.PartitioningType;
import util.GraphUtil;
import util.TestsUtil;
import util.Util;
public class ASPTestGridInterpreter 
{
	static int sizeOfBasicGraph = 70;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 100;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(12345);//
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
		
	static private int dim = 14;		
	public static void main(String[] args) 
	{
	
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(50);
		SimpleGraph<Node,Border> G = generator.generate(dim,rand);
		//6, 21 nodes
		//Generating the constraint graph
		//final GraphPartitioningState C  =GraphUtil.generateChainGraph(5);//getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
		final GraphPartitioningState C = TestsUtil.readConstraintGraphs("G:\\GitHub\\ASP_ConstrainedGraphPartitioning\\ASP_ConstrainedGraphPartitioning\\src\\java\\tests\\test_graphs\\tsmith.in").get(0);
		
		String file = "G:\\GitHub\\ASP_ConstrainedGraphPartitioning\\ASP_ConstrainedGraphPartitioning\\src\\java\\tests\\files\\result.out";
		List<String> answerSet = new LinkedList<String>();
		Scanner scan = null;
		try {
			scan = new Scanner(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		List<String> allLines = new LinkedList<>();  
		StringTokenizer tokenizer = new StringTokenizer(scan.nextLine(), " ");
        while(tokenizer.hasMoreTokens())
        {
        	String line = tokenizer.nextToken().trim();
        	//System.out.println(line);
            if (line.startsWith("%") || line.startsWith("SATISFIABLE")) {
                continue;
            }
            answerSet.add(line);
        }
        System.out.println(answerSet);
        
		GraphPartitioningState result = buildPartitioning(answerSet,G,C);
		ArrayList<Node> cells =  GraphUtil.getNodesArrayList(G);
		for(Partition p : result.vertexSet())
		{
			Color color =  new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),1);
			for(Node c : p.getAllCells())
			{
				for(Node n : cells)
				{
					if(c.getValue() == n.getValue())
					{
						CellNode cn = (CellNode)n;
						cn.cell.color = color;
						break;
					}
				}
			}
		}		
		
		for(Node c : result.getRemoved())
		{
			for(Node n : cells)
			{
				if(c.getValue() == n.getValue())
				{
					CellNode cn = (CellNode)n;
					cn.cell.color = Color.WHITE;
					break;
				}
			}
		}
		
		
		TestsUtil.colorizeRandom(result,Color.WHITE);
		generator.startDrawing(G,drawText);		
		
		/*
		SimpleGraph<Node,Border> uncoarsenedG  = G;
		String bordersJson = getBordersJSON(answerSet);
		String basicGraphJson = toJSON(G,uncoarsenedG,dim,dim);	
		String constraintGraphJson = toJSON(C,C.getNamesMap());
		String partitionsJson =  getPartitionsJSON(result,C.getNamesMap(),uncoarsenedG==null?G:uncoarsenedG);
		
		System.out.println(basicGraphJson);
		System.out.println(constraintGraphJson);
		System.out.println(bordersJson);
		System.out.println(partitionsJson);
		*/
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
		//GraphUtil.buildQuotientGraph(Q, G);
		return Q;
	}
	public static void buildQuotientGraph(GraphPartitioningState quotientGraph, SimpleGraph<Node, Border> basicGraph) 
	{
		final Partition[] newPars = GraphUtil.getPartitions(quotientGraph);
		for (int p = 0; p < newPars.length; p++) 
		{		
			// get all the borders of the members
			TreeSet<Border> set = new TreeSet<Border>();
			for (int m = 0; m < newPars[p].getMembers().size(); m++) {
				set.addAll(basicGraph.edgesOf(newPars[p].getMembers().get(m)));
			}

			// find if any border starts and ends in two different partitions,
			// if so ,add to the edges
			Iterator<Border> it = set.iterator();
			while (it.hasNext()) {
				Border bor = it.next();
				if (quotientGraph.removedContains(bor.getN1()) || quotientGraph.removedContains(bor.getN2()))
					continue;
				
				PartitionNodePair pair1 = GraphUtil.findPartition(bor.getN1(), newPars);
				PartitionNodePair pair2 = GraphUtil.findPartition(bor.getN2(), newPars);

				if (!pair1.getContainer().equals(pair2.getContainer())) 
				{
					PartitionBorder parBorder = new PartitionBorder(pair1.getContainer(), pair2.getContainer());
					quotientGraph.addEdge((Partition) parBorder.getP1(), (Partition) parBorder.getP2(), parBorder);
					// /above we made sure the edge link two different
					// partitions,
					// /next we add the node to the neighbors if its container
					// is not the partition
					if (pair1.getContainer().equals(newPars[p])) {
						pair2.getContainer().addNeighbor(pair1.getNode());
						newPars[p].addNeighbor(pair2.getNode());
					}
					else // then newPars[p] is the container of getN2()
					{
						newPars[p].addNeighbor(pair1.getNode());
						pair1.getContainer().addNeighbor(pair2.getNode());
					}
				}
			}

		}
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
	
	static String getPartitionsJSON(GraphPartitioningState state,Map<Integer,String> namesMap, SimpleGraph<Node,Border> G)
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
				ArrayList<Node> tmp = v.getSelfOrCluster();
				for(Node n : tmp)
				{
					nodes.put(n.getValue());
				}				
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

			ArrayList<Node> neighborsArr = getAllNeighborCells(partitions[i]);
			Map<Node,ArrayList<Node>> nodeToNeighborsMap =  new HashMap<>();
			JSONArray nodes = new JSONArray();
			for(Node v : partitions[i].getAllCells())
			{		
				nodes.put(v.getValue());
				ArrayList<Node> adjs = GraphUtil.adjacentsInMembersTo(G, neighborsArr, v);
				if(!adjs.isEmpty())
				{
					nodeToNeighborsMap.put(v,adjs);
				}
			}
			partitionObject.put("nodes", nodes);
			
			JSONArray neighbors = new JSONArray();
			HashMap<Integer, Set<Node>> map = new HashMap<>();
			for(Node member : nodeToNeighborsMap.keySet())
			{		
				for(Node neighbor : nodeToNeighborsMap.get(member))
				{
					if(!map.containsKey(neighbor.getContainer().getNumber()))
					{
						map.put(neighbor.getContainer().getNumber(), new HashSet<Node>());
					}
					map.get(neighbor.getContainer()).add(neighbor);
				}
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

			partitionObject.put("neighborNodes", neighbors);
			partitionObject.put("adjacentPartitions",map.keySet().toArray());
			
			
			arr.put(partitionObject);
		}
			
		obj.put("partitions",arr);
		return obj.toString();
	}
	public static ArrayList<Node> getAllNeighborCells(Partition par)
	{
		ArrayList<Node> cells = new ArrayList<Node>(par.getNumAllCells());		
		for(Node neighbor : par.getNeighbors())
		{
			ArrayList<Node> arr = neighbor.getSelfOrCluster();
			for(Node v : arr)
			{
				v.setContainer(neighbor.getContainer());
				cells.add(v);
			}				
		}
		return cells;
	}
	private static String toJSON(SimpleGraph<Node, Border> G, SimpleGraph<Node, Border> uncoarsenedG, int w, int h)
	{
		JSONObject obj = new JSONObject();
		JSONObject graph = new JSONObject();
		Collection<Node> tmp;
		if(uncoarsenedG != null)
		{
			tmp = GraphUtil.getUncoursenedNodes(G);
		}
		else
		{
			tmp = G.vertexSet();
		}
		
		JSONArray nodes = new JSONArray();
		for(Node v : tmp)
		{
			JSONObject node = new JSONObject();
			node.put("id", v.getValue());
			Partition par = v.getContainer();
			node.put("partition", par == null?0:par.getNumber());//So partitions numbers start from 1, and 0 be for removed par		
			//TODO generalize for Voronoi as well
			//CellNode cellNode = (CellNode)v;			
			//node.put("x", cellNode.cell.getX());
			//node.put("y", cellNode.cell.getY());
			node.put("category", "");
			JSONArray edges = new JSONArray();	
			for(Border e : (uncoarsenedG == null?G:uncoarsenedG).edgesOf(v))
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
