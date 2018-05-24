package asp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import org.jgrapht.graph.SimpleGraph;

import examples.GridGenerator;
import search.basic.Border;
import search.basic.ConstrainedGraphPartitioning;
import search.basic.GraphPartitioningState;
import search.basic.Node;
import search.basic.SearchConfiguration;
import util.GraphUtil;

public class AStar_ASP_Test 
{
	static int gridDim = 9;
	static int sizeMax = 40;
	static int sizeInc = 2;
	static int sizeInit = 40;
	static int numRuns = 1; 
	static int afterCoarseningSize = -1;//-1 for no coarsening
	static boolean allowNodeRemoval = true;
	static Random rand =  new Random(42);
	public static void main(String[] args)
	{
		//Basic Graph
		GridGenerator generator = new GridGenerator();
		generator.setupGenerator(50);
		SimpleGraph<Node,Border> G = generator.generate(gridDim,null);
		
		
		ArrayList<RunResult> results = new ArrayList<RunResult>();
		for(int size = sizeInit; size <= sizeMax;size += sizeInc)
		{
			//Constraint Graph
			GraphPartitioningState C  = GraphUtil.generateChainGraph(size);
			int numNulls = 0;
			for(int i = 0; i < numRuns;i++)
			{
				long t = System.currentTimeMillis();
				ASPConstrainedGraphPartitioning asp = new ASPConstrainedGraphPartitioning(G,C,allowNodeRemoval,0,1);
				GraphPartitioningState result =	asp.partition();
				//GraphPartitioningState result = ConstrainedGraphPartitioning.partitionConstrainedWithCoarseningAndRandomRestart(new SearchConfiguration(G, C),rand, 20, 50, afterCoarseningSize);
				if(result != null)
					results.add(new RunResult(size,(System.currentTimeMillis() - t)));
				else
					numNulls++;
				System.out.println(i);
			}
			System.out.println("Done with size= "+size + ", numNulls = "+numNulls);
		}
		writeToCSV(results,"asp_4.csv",true);
	}

	static class RunResult
	{
		private int size;
		public int getSize() {
			return size;
		}

		public long getTime() {
			return time;
		}

		private long time;

		RunResult(int size, long time)
		{
			this.size = size;
			this.time = time;
		}
	}
	private static void writeToCSV(ArrayList<RunResult> runsArray,String csvFilename,boolean appendToFile)
	{
		PrintWriter pw;
		try {
			
			pw = new PrintWriter(new FileOutputStream(csvFilename,appendToFile));//true for appending
		
	        StringBuilder sb = new StringBuilder();
	        if(!appendToFile)
	        {
		        sb.append("size");
	        	sb.append(',');
	        	sb.append("time (ms)");
	        	sb.append('\n');
	        }
	        for(RunResult run : runsArray)
			{
	        	sb.append(run.size);
	        	sb.append(',');
	        	sb.append(run.time);
	        	sb.append('\n');
			}
	        pw.write(sb.toString());
	        pw.close();
			System.out.println("#Data Writter to the file = " + csvFilename);

		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
