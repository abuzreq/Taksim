package examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GridGraphGenerator;
import org.jgrapht.graph.SimpleGraph;

import examples.GridGenerator.CellNode;
import examples.VoronoiGenerator.FaceNode;
import examples.VoronoiGenerator.Map.Face;
import processing.core.PApplet;
import search.basic.BasicGraphGenerator;
import search.basic.Border;
import search.basic.Node;
import util.GraphUtil;

public class GridGenerator implements BasicGraphGenerator
{
	public int dim;
	public int cellDim;
	protected static GridGenerator instance;
	public GridGenerator() 
	{
		instance = this;
	}
	
	@Override
	public SimpleGraph<Node, Border> generate(int dim, Random rand)
	{
		this.dim = dim;

		VertexFactory<Node> vf = new VertexFactory<Node>()
		{
			int num = -1;
			@Override
			public Node createVertex() 
			{
				num++;
				return new CellNode(null,num);
			}
		};
		SimpleGraph<Node,Border> graph = generateBasicGridGraph(dim,vf);
		Node[] nodes = getNodes(graph);
		
		for(int i = 0 ; i < dim;i++)
		{
			for(int j = 0; j < dim ;j++)
			{
				int n = i + dim*j;
				CellNode cn = (CellNode)nodes[n];
				int x = i*cellDim;
				int y = j*cellDim;
				Cell cell = new Cell(x,y);
				cn.cell = cell;
			}
		}		
		
		return graph;
	}

	/**
	 * This shows how can you integrate the library in your application, 
	 * in this case FaceNode is a Node in the basic graph but also contains 
	 * a reference to a generated Voronoi polygon face
	 * @author abuzreq
	 *
	 */
	public class CellNode extends Node
	{
		public Cell cell;

		public CellNode(Cell cell, int value) {
			super(value);
			this.cell = cell;
			
		}
		public CellNode(int value,ArrayList<Node> cluster) {
			super(value,cluster);			
		}

		@Override
		public CellNode clone()
		{
			CellNode fn = new CellNode(cell, getValue());
			if(!getCluster().isEmpty())
			{
				fn.getCluster().addAll(getCluster());
			}
			return  fn;
		}
	}
	public class Cell
	{
		public Color color;
		private int x,y;

		public int getY() {
			return y;
		}

		public int getX() {
			return x;
		}

		public Cell(int x, int y) 
		{
			this.x = x;
			this.y = y;
		}
		
	}
	private static Node[] getNodes(Graph<Node, Border> G) {
		Set<Node> vertexSet = G.vertexSet();
		Node[] pars = new Node[vertexSet.size()];
		vertexSet.toArray(pars);
		return pars;
	}
	private static SimpleGraph<Node,Border> generateBasicGridGraph(int n, VertexFactory<Node> vf)
	{

		EdgeFactory<Node,Border> bf =  new EdgeFactory<Node,Border>()
		{

			@Override
			public Border createEdge(Node sourceVertex, Node targetVertex)
			{
				Border b = new Border(sourceVertex,targetVertex);
				return b;
			}
			
		};
		
		SimpleGraph<Node,Border> G = new SimpleGraph<Node,Border>(bf);
		GridGraphGenerator<Node,Border> gg = new GridGraphGenerator<Node,Border>(n, n);
		gg.generateGraph(G, vf,null);
		
		return G;
	}
	
	private Node[] nodes;
	public void draw(GridGeneratorDrawer applet,boolean drawText) 
	{
		
		for(int i = 0; i < nodes.length;i++)
		{
			ArrayList<Node> cluster = nodes[i].getCluster();
			if(cluster.isEmpty())
			{
				drawNode(applet,drawText,nodes[i]);
			}
			else
			{
				for(int j = 0 ; j < cluster.size();j++)
				{
					drawNode(applet,drawText,cluster.get(j));
				}
			}		
		}	
	}
	void drawNode(PApplet applet,boolean drawText,Node node)
	{
		if(node.isClusterEmpty())
		{
			CellNode cn = (CellNode)node;
			int x = cn.cell.getX();
			int y = cn.cell.getY();
			Color c = cn.cell.color;
			applet.strokeWeight(0);
			applet.stroke(255);
	
			applet.fill(c.getRed(), c.getGreen(), c.getBlue());
			applet.rect(x,y,cellDim,cellDim); 
			if(drawText)
			{
				applet.fill(0);
				applet.text(node.getValue(), x+cellDim/2, y+cellDim/2);
			}
		}
		else
		{
			ArrayList<Node> cluster = node.getCluster();
			for(int j = 0 ; j < cluster.size();j++)
			{
				drawNode(applet,drawText,cluster.get(j));
			}
		}
	}
	public void setupGenerator( int cellDim) 
	{
		this.cellDim  = cellDim;
		
	}

	public void startDrawing(SimpleGraph<Node,Border> G, boolean drawText) 
	{
		Node[] nodes =  GraphUtil.getNodes(G);
		this.nodes = nodes;
		PApplet.main("examples.GridGeneratorDrawer");
		GridGeneratorDrawer.instance.drawText = drawText;
	
	}


}
