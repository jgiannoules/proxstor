package com.giannoules.proxstor;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import java.util.Map;

/**
 * 
 * hold ProxStor instance-wide reference to Graph
 * 
 */
public enum ProxStorGraph {
    instance;
    
    private Graph graph;
   
    /*
     * must use createGraph(...)
     */
    ProxStorGraph() { }
   
    /*
     * preferred use of GraphFactory
     */
    public void createGraph(Map<String, String> conf) {
        graph = GraphFactory.open(conf);
    }
   
  
    public void createGraph(Graph g) {
        graph = g;
    }
  
    /*
     * default to simple TinkerGraph
     */
    public void createGraph() {
        graph = new TinkerGraph();
    }
    
    /*
     * shutting down Graph instance should flush all commits to disk
     */
    public void shutdown() {
        if (graph != null) {
            if (graph instanceof Neo4jGraph) {
                ((Neo4jGraph) graph).commit();
            }
            graph.shutdown();
            graph = null;
            OrientGraph o;
        }
    }
    
    public Graph getGraph() {        
        return instance.graph;
    }
    
    /*
     * "running" is whether non-null Graph instance exists
     */
    public boolean isRunning() {
        return (graph != null);
    }
    
    public Vertex newVertex() {
        return graph.addVertex(null);
    }
    
    public Edge newEdge(Vertex outVertex, Vertex inVertex, String label) {
        return graph.addEdge(null, outVertex, inVertex, label);
    }
    
    public Vertex getVertex(Object id) {
        return graph.getVertex(id);
    }
    
    public void commit() {
        if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).commit();
            }
    }
    
    private void _TinkerGraph() {
        graph = new TinkerGraph();
    }
    
    private void _Neo4jGraph() {
        graph = new Neo4jGraph("/tmp/abcd");
    }
    
    private void _Neo4j2Graph() {
        graph = new Neo4j2Graph("/tmp/abcd");
    }
    
    
    @Override
    public String toString() {
        return graph.toString();
    }
    
}
