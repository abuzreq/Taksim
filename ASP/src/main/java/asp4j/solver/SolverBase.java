package asp4j.solver;

import asp4j.lang.AnswerSet;
import asp4j.lang.AnswerSetImpl;
import asp4j.lang.Atom;
import asp4j.util.ParseUtils;
import asp4j.program.Program;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author hbeck Apr 14, 2013
 */
public abstract class SolverBase implements Solver {

    protected File inputFile;
    protected int lastProgramHashCode;
    protected List<AnswerSet<Atom>> lastProgramAnswerSets;

    String extraParams;//Added by Ahmed Abuzuraiq
    public void setExtraParams(String extraParams)
    {
    	this.extraParams =extraParams;
    }
    
    public SolverBase() {
        inputFile = null;
        lastProgramAnswerSets = null;
    }

    /**
     * command to start the solver, including params (excluding input programs)
     *
     * @return part before list of files
     */
    protected abstract String solverCommand();

    /**
     * @return list of strings, each representing an answer set
     */
    protected abstract List<String> getAnswerSetStrings(Process exec) throws IOException;
    protected abstract List<List<String>> getAnswerSetStrings(Process exec,int num) throws IOException;
    /**
     * prepare answer set string for tokenization. e.g. surrounding braces may
     * be removed.
     *
     * @param answerSetString
     * @return string to tokenize based on standard configuration
     * @see anwerSetDelimiter
     */
    protected abstract String prepareAnswerSetString(String answerSetString);

    /**
     * @return separator of atoms withing an answer set
     */
    protected abstract String atomDelimiter();

    //
    //  std implementations
    //
    protected void clear() {
        lastProgramAnswerSets = null;
    }

    public List<List<String>> getAnswerSetsAsStrings(Program<Object> program,int num) throws SolverException 
    {
        preSolverExec(program);
        try {
            Process exec = Runtime.getRuntime().exec(solverCallString(program));
            //exec.waitFor();
            List<List<String>> answerSetStrings = getAnswerSetStrings(exec,num);
            postSolverExec(program);
            return  Collections.unmodifiableList(answerSetStrings);
        } catch (IOException e) {
          throw new SolverException(e);
        } 
    }

    public List<String> getAnswerSetsAsStrings(Program<Object> program) throws SolverException 
    {
        preSolverExec(program);
        try {
            Process exec = Runtime.getRuntime().exec(solverCallString(program));
            //exec.waitFor();
            List<String> answerSetStrings = getAnswerSetStrings(exec);
            postSolverExec(program);
            return  Collections.unmodifiableList(answerSetStrings);
        } catch (IOException e) {
          throw new SolverException(e);
        } 
    }


    /**
     * maps a list of answer sets, represented as strings, to a list of (low
     * level) AnswerSet objects
     */
    protected List<AnswerSet<Atom>> mapAnswerSetStrings(List<String> answerSetStrings) throws ParseException {
        List<AnswerSet<Atom>> answerSets = new ArrayList<>();
        for (String answerSetString : answerSetStrings) {
            answerSetString = prepareAnswerSetString(answerSetString);
            String[] atomStrings = answerSetString.split(atomDelimiter());
            Set<Atom> atoms = new HashSet<>();
            for (String atomString : atomStrings) {
                atoms.add(ParseUtils.parseAtom(atomString));
            }
            answerSets.add(new AnswerSetImpl<>(atoms));
        }
        return answerSets;
    }

    @Override
    public Set<Atom> getConsequence(Program<Atom> program, ReasoningMode mode) throws SolverException {
        List<AnswerSet<Atom>> as = getAnswerSets(program);
        switch (mode) {
            case BRAVE:
                return braveConsequence(as);
            case CAUTIOUS:
                return cautiousConsequence(as);
            default:
                return null;
        }
    }

    protected Set<Atom> cautiousConsequence(List<AnswerSet<Atom>> answerSets) {
        Set<Atom> intersection = new HashSet<>();
        Iterator<AnswerSet<Atom>> it = answerSets.iterator();
        if (it.hasNext()) {
            intersection.addAll(it.next().atoms());
            while (it.hasNext()) {
                intersection.retainAll(it.next().atoms());
            }
        }
        return Collections.unmodifiableSet(intersection);
    }

    protected Set<Atom> braveConsequence(List<AnswerSet<Atom>> answerSets) {
        Set<Atom> set = new HashSet<>();
        for (AnswerSet<Atom> answerSet : answerSets) {
            set.addAll(answerSet.atoms());
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     *
     * @param program
     * @return full call to solver, i.e., solver command plus programs
     * @throws IOException
     */
    protected String solverCallString(Program<Object> program) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(solverCommand());
        for (File file : program.getFiles()) {
            sb.append(" ").append(file.getAbsolutePath());
        }
        if (!program.getInput().isEmpty()) {
            sb.append(" ").append(inputFile.getAbsolutePath());
        }
        
        System.out.println("CMD>> "+sb.toString() + extraParams);     
        return sb.toString()+extraParams;
    }

    protected File tempInputFile() throws IOException {
        if (inputFile == null) {
            inputFile = File.createTempFile("asp4j-tmp-prog-", ".lp");
            inputFile.deleteOnExit();
        }
        return inputFile;
    }

    /**
     * executed before call to solver
     */
    protected void preSolverExec(Program<Object> program) throws SolverException {
        Collection<Object> inputAtoms = program.getInput();
        if (inputAtoms.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object atom : inputAtoms) {
            sb.append(atom.toString());
        }
        try {
            FileUtils.writeStringToFile(tempInputFile(),sb.toString());
        } catch (IOException ex) {
            throw new SolverException(ex);
        }
    }

    /**
     * executed after call to solver
     */
    protected void postSolverExec(Program<Object> program) {
    }
}
