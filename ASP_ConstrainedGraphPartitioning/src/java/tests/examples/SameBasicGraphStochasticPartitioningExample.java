package examples;

import java.awt.Color;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;

public class SameBasicGraphStochasticPartitioningExample {

	static int sizeOfBasicGraph = 9;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = -1;
	
	static Random rand =  new Random(42);
	public static void main(String[] args) 
	{
		//VoronoiGenerator generator = new VoronoiGenerator();
		GridGenerator generator = new GridGenerator();
		//Generating the constrain graph
		//final GraphPartitioningState C  = GraphUtil.generateChainGraph(5);
		final GraphPartitioningState C = TestsUtil.readConstraintGraphs("G:\\GitHub\\ASP_ConstrainedGraphPartitioning\\ASP_ConstrainedGraphPartitioning\\src\\java\\tests\\test_graphs\\zelda.in").get(0);
		GraphPartitioningState result = null;
		//Setting up the generator and generating the basic graph
		//generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		generator.setupGenerator(50);
		
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
		long t = System.currentTimeMillis();
		result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarseningAndRandomRestart(new SearchConfiguration(G, C),rand, initialLimitOnMaxNodesExpanded, increamentInLimit, afterCoarseningSize);
		System.out.println((System.currentTimeMillis() - t)/1000.0);
		System.out.println("Result Found");
		//System.out.println(result);
		generator.startDrawing(G, false);
		TestsUtil.colorizeRandom(result,Color.WHITE);
		
	}

}
