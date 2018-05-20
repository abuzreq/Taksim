package asp;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

import examples.VoronoiGenerator;
import examples.VoronoiGenerator.FaceNode;
import processing.core.PVector;
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
	static int sizeOfBasicGraph = 60;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(42);
	static int timeLimit = 0;// 0 for no limit, allow enough time for atleast one solution to be found, the extra time is to limit optimization
	static int numModels = 1;  //0 for finding the optimal model
	
	private final static String rulefile_edges = System.getProperty("user.dir") + "/src/java/tests/files/edges.lp";
	private final static String rulefile_constraints = System.getProperty("user.dir") + "/src/java/tests/files/adjacency_constraints.lp";
	private final static String rulefile_node_constraints = System.getProperty("user.dir") + "/src/java/tests/files/node_constraints.lp";
	
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
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		SimpleGraph<Partition,PartitionBorder> Cc  = new SimpleGraph<Partition,PartitionBorder>(bf);//GraphUtil.generateChainGraph(8);
		GnmRandomGraphGenerator<Partition,PartitionBorder> gen = new GnmRandomGraphGenerator<>(7, 10,1234);
		gen.generateGraph(Cc, vf, null);
		System.out.println(Cc);
		GraphPartitioningState C = new GraphPartitioningState();
		for(Partition p: Cc.vertexSet())
		{
			C.addVertex(p);
		}
		for(PartitionBorder pb: Cc.edgeSet())
		{
			C.addEdge(pb.getP1(),pb.getP2(), pb);
		}
		String filePath = ""; 
		C  =GraphUtil.generateChainGraph(10);// getC();//TestsUtil.readConstraintGraphs(filePath).get(0);
		
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		
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
		*/
		System.out.println("# of Nodes = "+GraphUtil.sizeOf(G));
		writeBasicGraphToFile(G,rulefile_edges);
		writeConstraintGraphToFile(C,rulefile_constraints);
		writeNodeInPartitionConstraintsToFile(node2par,rulefile_node_constraints);

		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,timeLimit,numModels);
		GraphPartitioningState result = asp.partition();
		System.out.println(result);
		TestsUtil.colorizeRandom(result,Color.WHITE);
		exportToSVG(result,G);
	}
	private static void exportToSVG(GraphPartitioningState Q, SimpleGraph<Node, Border> G)
	{
		GeometryFactory factory = new GeometryFactory();
		Partition[] pars = GraphUtil.getPartitions(Q);
		Polygon[] fullPolygons = new Polygon[pars.length];
		//List<Geometry> fullPolygons2 = new LinkedList<Geometry>();
		for(int i =0 ; i < pars.length;i++)
		{
			ArrayList<Node> nodes = pars[i].getAllCells();
			List<Polygon> polygons = new LinkedList<Polygon>();
			for(int j =0 ; j < nodes.size();j++)
			{
				FaceNode faceNode = (FaceNode)(nodes.get(j));
				Coordinate[] coords = toCoords(faceNode.face.getPoints());			
				CoordinateSequence coordsSeq = new CoordinateArraySequence(coords);			
				LinearRing shell = new LinearRing(coordsSeq, factory);
				polygons.add(new Polygon(shell, null, factory));
			}

			CascadedPolygonUnion unionSamurai = new CascadedPolygonUnion(polygons);
			Geometry geom = unionSamurai.union();
			fullPolygons[pars[i].getNumber()-1] = (Polygon)geom;
			//fullPolygons2.add(geom);
		}
		SimpleSvgRenderer renderer = new SimpleSvgRenderer(1000, 1000, 1, 1);
		renderer.renderPolygonPointList(fullPolygons);
		//renderer.renderLineList(fullPolygons2);
		try {
			renderer.export("map.svg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(Arrays.deepToString(fullPolygons));
		
	}
	private static Coordinate[] toCoords(ArrayList<PVector> points)
	{
		Coordinate[] coords = new Coordinate[points.size()+1];
		for(int i =0 ; i < points.size();i++)
		{
			coords[i] = new Coordinate(points.get(i).x, points.get(i).y);
		}
		coords[coords.length-1] =  new Coordinate(points.get(0).x, points.get(0).y);
		return coords;
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
	

	private static class SimpleSvgRenderer {

        private final StringBuilder svgContent = new StringBuilder();
        private final StringBuilder defsContent = new StringBuilder();
        private final int width;
        private final int height;
        private final double ratioX;
        private final double ratioY;

        public SimpleSvgRenderer(int width, int height, double ratioX, double ratioY) {
            this.width = width;
            this.height = height;
            this.ratioX = ratioX;
            this.ratioY = ratioY;
        }

        public void renderLineList(List<Geometry> lineList) {

            svgContent.append("<svg:path class='line' d='");

            for (Geometry geometry : lineList) {

                Coordinate[] coordinates = geometry.getCoordinates();

                svgContent.append("M");
                svgContent.append((int) (coordinates[0].x * ratioX));
                svgContent.append(" ");
                svgContent.append((int) (coordinates[0].y * ratioY));

                for (int i = 1; i < coordinates.length; i++) {
                    svgContent.append("L");
                    svgContent.append((int) (coordinates[i].x * ratioX));
                    svgContent.append(" ");
                    svgContent.append((int) (coordinates[i].y * ratioY));
                }
            }

            svgContent.append("'/>");
        }
        public void renderPolygonPointList(Polygon[] polygons)
        {
        	int num = 1;
        	for (Polygon poly : polygons) 
        	{       	
        		svgContent.append("<svg:path class='polygon' d='");     
                boolean isFirst = true;
                for (Coordinate point : poly.getCoordinates()) 
                {
                    if (isFirst) {
                        svgContent.append("M");
                        isFirst = false;
                    } else {
                        svgContent.append("L");
                    }
                    svgContent.append((int) (point.x * ratioX));
                    svgContent.append(" ");
                    svgContent.append((int) (point.y * ratioY));
                }
                svgContent.append("' id='Territory_"+num+"'");
                svgContent.append("/>");
                num += 1;
            }

            
        }
        public void renderPolygonPointList(List<Point2D.Double>[] polygonPointList) {

            svgContent.append("<svg:path class='polygon' d='");

            for (List<Point2D.Double> contourData : polygonPointList) {
                boolean isFirst = true;
                for (Point2D.Double point : contourData) {
                    if (isFirst) {
                        svgContent.append("M");
                        isFirst = false;
                    } else {
                        svgContent.append("L");
                    }
                    svgContent.append((int) (point.x * ratioX));
                    svgContent.append(" ");
                    svgContent.append((int) (point.y * ratioY));
                }
            }

            svgContent.append("'/>");
        }

        private void writeCss() {
            defsContent.append(" .line { stroke: green; fill: none;}");
            defsContent.append(" .polygon { stroke: red; fill: silver;}");
        }

        public void export(String filePath) throws IOException {

            StringBuilder header = new StringBuilder();
            header.append("<?xml version='1.0' encoding='utf-8'?>");
            header.append("<svg:svg xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:svg='http://www.w3.org/2000/svg' version='1.1' preserveAspectRatio='none' width='");
            header.append(width);
            header.append("' height='");
            header.append(height);
            header.append("' viewBox='0 0 ");
            header.append(width);
            header.append(" ");
            header.append(height);
            header.append("'>");

            writeCss();

            String defs = "<svg:defs><svg:style type='text/css'><![CDATA[" + defsContent + "]]></svg:style></svg:defs>";

            svgContent.insert(0, header + defs);
            svgContent.append("</svg:svg>");

            try (
                    OutputStream outputStream = new FileOutputStream(filePath);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {

                writer.write(svgContent.toString());
            }
        }
    }
}
