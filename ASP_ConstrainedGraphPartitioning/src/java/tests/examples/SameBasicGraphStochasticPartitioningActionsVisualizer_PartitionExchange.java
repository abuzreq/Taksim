package examples;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.search.framework.evalfunc.HeuristicFunction;
import aima.core.search.framework.problem.ActionsFunction;
import aima.core.search.framework.problem.GoalTest;
import search.actions.NodeRemovalAction;
import search.actions.PartitionChangeAction;
import search.actions.PartitionExchangeAction;
import search.basic.Border;
import search.basic.ConstrainedGraphPartitioningReturnActions;
import search.basic.ConstrainedGraphPartitioningReturnActions.InitialStateActionsPair;
import search.enums.PartitioningType;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.Partition;
import search.basic.PartitionBorder;
import search.basic.SearchConfiguration;
import util.GraphUtil;
import util.TestsUtil;


/**
 * Press Right arrow to get the next state, left arrow to get the previous
 * 
 * @author abuzreq
 *
 */
public class SameBasicGraphStochasticPartitioningActionsVisualizer_PartitionExchange {

	static int sizeOfBasicGraph = 200;
	static int initialLimitOnMaxNodesExpanded = 10;
	static int increamentInLimit = 50;
	static int afterCoarseningSize = 60;
	
	static Random rand =  new Random(1234);
	public static void main(String[] args) 
	{
		VoronoiGenerator generator = new VoronoiGenerator();
		//Generating the constrain graph
		final GraphPartitioningState C  = GraphUtil.generateChainGraph(7);
		//Setting up the generator and generating the basic graph
		generator.setupGenerator(sizeOfBasicGraph, true, false, 500, 500, false, false, false);
		SimpleGraph<Node,Border> G = generator.generate(sizeOfBasicGraph,rand);
		SearchConfiguration sc = new SearchConfiguration(G, C);
		
		GraphPartitioningState Q = GraphUtil.partition(G, GraphUtil.sizeOf(C), PartitioningType.KERNEL_DETERMINISTIC, new Random(0), false);
		System.out.println(C);
		System.out.println("agaa "+GraphUtil.numUnqualEdges(C.edgeSet(), Q.edgeSet()));	
		System.out.println("agaa "+GraphUtil.numUnqualEdges(C.edgeSet(), C.edgeSet()));	
		System.out.println(Q);
		//System.exit(0);
		sc.setActionsFunction( new ActionsFunction() {
			
			public Set<Action> actions(Object state) {
				Set<Action> actions = new LinkedHashSet<Action>();
				Partition[] partitions = GraphUtil.getPartitions((SimpleGraph<Partition, PartitionBorder>) state);
				for (int p = 0; p < partitions.length; p++)
				{
					for (int q = 0; q < partitions.length; q++)
					{
						
						if(!partitions[p].equals(partitions[q]))
						{

							PartitionExchangeAction a = new PartitionExchangeAction(partitions[p],partitions[q]);
							if(!actions.contains(a))
							{
								actions.add(a);
								//System.out.println(partitions[p].getNumber() + " "+partitions[q].getNumber() );
							}
						}
					}
				}
				for (int p = 0; p < partitions.length; p++) 
				{
					Partition par = partitions[p];
					ArrayList<Node> neighbors = par.getNeighbors();
					for (int i = 0; i < neighbors.size(); i++) 
					{
						if (neighbors.get(i).getContainer().getNumMembers() > 1) 
						{
							if (sc.isPreventGaps()) 
							{
								if (!GraphUtil.isCreatingAGap(neighbors.get(i), sc.getBasicGraph())) 
								{
									if (sc.isAllowNodeRemoval()) 
									{
										actions.add((Action) new NodeRemovalAction(neighbors.get(i)));
									}
									actions.add((Action) new PartitionChangeAction(par, neighbors.get(i)));
								}

							} else {
								if (sc.isAllowNodeRemoval()) {
									actions.add((Action) new NodeRemovalAction(neighbors.get(i)));
								}
								actions.add((Action) new PartitionChangeAction(par, neighbors.get(i)));
							}
						}

					}
				}
				return actions;
			}

		});

		
		sc.setGoalTest(new GoalTest() {
			
			@Override
			public boolean isGoalState(Object state) 
			{
				GraphPartitioningState result =( GraphPartitioningState) state;
				return GraphUtil.numUnqualEdges(result.edgeSet(), C.edgeSet()) ==0;//GraphUtil.equalAdjacencies(result,sc.getConstraintGraph());
			}
		});
		
		/*
		sc.setHeuristicFunction(new HeuristicFunction() {
			public double h(Object state)
			{
				GraphPartitioningState graph = (GraphPartitioningState) state;
				return  GraphUtil.numUnqualEdges(graph.edgeSet(), C.edgeSet());			
			}
			@Override
			public String toString() {
				return "(Wrong Edges Error) ";
			}
		});
		*/
		InitialStateActionsPair result = null;//new SearchConfiguration(G, C)
		result = ConstrainedGraphPartitioningReturnActions.partitionConstrainedWithCoarseningAndRandomRestart(sc,rand, initialLimitOnMaxNodesExpanded, increamentInLimit,afterCoarseningSize);	
		System.out.println("Result Found");
		//System.out.println(result);
		current = result.getInitialState();
		actions  = result.getActions();
		System.out.println(actions);
		SimpleGraph<Node,Border> lastUsedG = result.getG();
		TestsUtil.colorizeFixed(current,Color.WHITE);
		states.add(current);

		Callable onRight = new Callable() {

			@Override
			public Object call() throws Exception
			{
				if(index  < states.size()-1)
				{
					index += 1;
					current = states.get(index);
					TestsUtil.colorizeFixed(current,Color.white);
				}
				else if(actions.size() != 0)
				{	
					Action a = actions.get(0);
					actions.remove(0);
					final GraphPartitioningState next;
					next = applyAction(lastUsedG,current,a);					
					states.add(next);
					current = next;
					index = states.size()-1;
					TestsUtil.colorizeFixed(current,Color.white);
				}
				return null;
			}
		};
		Callable onLeft = new Callable() {
			@Override
			public Object call() throws Exception
			{
				index = index -1;
				if(index < 0)
					index = 0;
				if(index >= states.size())
				{
					index = states.size()-1;
				}
				current = states.get(index);
				TestsUtil.colorizeFixed(current,Color.white);
				return null;
			}
		};
		VoronoiGeneratorDrawer.onRight = onRight;
		VoronoiGeneratorDrawer.onLeft = onLeft;
	}
	static int index = 0;
	static List<Action> actions;
	static ArrayList<GraphPartitioningState> states = new ArrayList<>() ;
	static GraphPartitioningState current;
	static GraphPartitioningState applyAction(SimpleGraph<Node,Border> G,GraphPartitioningState s,Action a)
	{
		List<Action> actions = new LinkedList<Action>();
		actions.add(a);
		GraphPartitioningState result =  GraphUtil.applyActions(s, G, actions);
		return result;
	}


}
