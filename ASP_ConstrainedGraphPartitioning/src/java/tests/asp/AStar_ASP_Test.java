package asp;

import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import examples.GridGenerator;
import search.basic.Border;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import util.GraphUtil;

public class AStar_ASP_Test 
{
	
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(1234);
	public static void main(String[] args)
	{
		//Basic Graph
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(50);
		SimpleGraph<Node,Border> G = generator.generate(7,null);
		
		//Constraint Graph
		GraphPartitioningState C  = GraphUtil.generateChainGraph(10);

		
		ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,0,1);
		GraphPartitioningState result  = asp.partition();
		
		

	}

}
