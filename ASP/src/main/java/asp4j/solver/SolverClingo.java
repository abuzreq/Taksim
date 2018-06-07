package asp4j.solver;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;

import asp4j.lang.AnswerSet;
import asp4j.lang.Atom;
import asp4j.program.Program;

/**
 *
 * @author hbeck  May 27, 2013
 */
public class SolverClingo extends SolverBase {

	private int numModels;
	public SolverClingo(int numModels)
	{
		this.numModels = numModels;
	}
    @Override
    protected String solverCommand() {
        return "clingo "+numModels+" --verbose=0";
    }

    protected List<String> getAnswerSetStrings(Process exec) throws IOException {
        InputStream inputStream = exec.getInputStream();
        List<String> allLines = IOUtils.readLines(inputStream);
        List<String> answerSetLines = new ArrayList<>();
        for (String line : allLines) 
        {
            if (line.startsWith("%") || line.startsWith("SATISFIABLE")|| line.contains("Optimization")|| line.contains("OPTIMUM") || line.contains("FOUND")) {
                continue;
            }
            answerSetLines.add(line);  
        }
        //Take the last answer set result, in case of optimizaion there can be many different ones, last is optimal or most optimal
        String[] lastLineWords = answerSetLines.get(answerSetLines.size()-1).split(" ");
        
        List<String> tmp = new ArrayList<>();
        for (String word : lastLineWords)
        {
        	tmp.add(word);
        }
        return tmp;
    }
    
    protected List<List<String>> getAnswerSetStrings(Process exec,int num) throws IOException {
        InputStream inputStream = exec.getInputStream();
        List<String> allLines = IOUtils.readLines(inputStream);
        List<String> answerSetLines = new ArrayList<>();
        for (String line : allLines) 
        {
            if (line.startsWith("%") || line.startsWith("SATISFIABLE")|| line.contains("Optimization")|| line.contains("OPTIMUM") || line.contains("FOUND")) {
                continue;
            }
            answerSetLines.add(line);  
        }
        List<List<String>> result = new LinkedList<List<String>> ();
        for(String line : answerSetLines)
        {
            String[] lastLineWords = line.split(" ");
            
            List<String> tmp = new ArrayList<>();
            for (String word : lastLineWords)
            {
            	tmp.add(word);
            }
            result.add(tmp);
        }
        System.out.println("Number of answersets = "+result.size());
        int n = result.size() - num;
        if(n < 0 || n >= result.size())
        	n = 0;
        return result.subList(n, result.size());
    }
    protected List<String> getAnswerSetStrings2(Process exec) throws IOException
    {
        InputStream inputStream = exec.getInputStream();
        System.out.println(" haha "+convertInputStreamToString(inputStream));
        String[] allLines = convertInputStreamToString(inputStream).split(" ");
        List<String> answerSetLines = new ArrayList<>();
        for (String line : allLines) {
            if (line.startsWith("%") || line.contains("SATISFIABLE") ) {
                continue;
            }
            answerSetLines.add(line);
        }
        return answerSetLines;
    }
    static String convertInputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    @Override
    protected String prepareAnswerSetString(String answerSetString) {
        return answerSetString;
    }

    @Override
    protected String atomDelimiter() {
        return " ";
    }
	@Override
	public List<AnswerSet<Atom>> getAnswerSets(Program<Atom> program) throws SolverException {
		// TODO Auto-generated method stub
		return null;
	}


}
