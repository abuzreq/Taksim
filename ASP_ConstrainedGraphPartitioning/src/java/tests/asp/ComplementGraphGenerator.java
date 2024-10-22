package asp;

/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

/**
 * Generator which produces the
 * <a href="http://mathworld.wolfram.com/GraphComplement.html">complement graph</a> of a given input
 * graph. The complement $\overline{G}$ of a graph $G$ consists of the same vertices as $G$, but
 * whose edge set consists of the edges not in $G$.
 * <p>
 * More formally, let $G = (V, E)$ be a graph and let $K$ consist of all 2-element subsets of $V$.
 * Then $\overline{G} = (V, K \setminus E)$ is the complement of $G$, where $K \setminus E$ is the
 * relative complement of $E$ in $K$. For directed graphs, the complement can be defined in the same
 * way, as a directed graph on the same vertex set, using the set of all 2-element ordered pairs of
 * $V$ in place of the set $K$ in the formula above.
 * <p>
 * The complement is not defined for multigraphs. If a multigraph is provided as input to this
 * generator, it will be treated as if it is a simple graph.
 *
 * @author Joris Kinable
 * @since Sept 2017
 *
 *	WARNING:: This is a modified version of 
 *https://github.com/jgrapht/jgrapht/blob/bb783a58c22355dc325e6587568a4427fd4d4513/jgrapht-core/src/main/java/org/jgrapht/generate/ComplementGraphGenerator.java
 *
 * In particular: it only support undirected graphs with no self loops
 * @param <V> vertex type
 * @param <E> edge type
 */
public class ComplementGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{

    private final Graph<V, E> graph;
    private final boolean generateSelfLoops;

    /**
     * Complement Graph Generator
     * 
     * @param graph input graph
     */
    public ComplementGraphGenerator(Graph<V, E> graph)
    {
        this(graph, false);
    }

    /**
     * Complement Graph Generator. If the target graph allows self-loops the complement of $G$ may
     * be defined by adding a self-loop to every vertex that does not have one in $G$. This behavior
     * can be controlled using the boolean <code>generateSelfLoops</code>.
     *
     * @param graph input graph
     * @param generateSelfLoops indicator whether self loops should be generated. If false, no
     *        self-loops are generated, independent of whether the target graph supports self-loops.
     */
    public ComplementGraphGenerator(Graph<V, E> graph, boolean generateSelfLoops)
    {
        this.graph = graph;
        this.generateSelfLoops = generateSelfLoops;
    }

    public void generateGraph(Graph<V, E> target, EdgeFactory<V,E> edgeFactory)
    {
        Graphs.addAllVertices(target, graph.vertexSet());



        List<V> vertices = new ArrayList<>(graph.vertexSet());
        for (int i = 0; i < vertices.size() - 1; i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                V u = vertices.get(i);
                V v = vertices.get(j);
                if (!graph.containsEdge(u, v))
                    target.addEdge(u,v,edgeFactory.createEdge(u, v));
            }
        }
        
        if (generateSelfLoops ) {
            for (V v : graph.vertexSet()) {
                if (!graph.containsEdge(v, v))
                    target.addEdge(v,v,edgeFactory.createEdge(v, v));
            }
        }
    }

	@Override
	public void generateGraph(Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap) {
		generateGraph(target,null,resultMap);
		
	}

}
