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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * hold ProxStor instance-wide reference to Graph and provide internal interface
 * to actual back-end instance
 * 
 * 
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
    public void createGraph(Map<String, String> conf) throws ProxStorGraphDatabaseAlreadyRunning {
        if (graph instanceof Graph) {
            throw new ProxStorGraphDatabaseAlreadyRunning("Graph instance already running");
        }
        graph = GraphFactory.open(conf);
    }     
    
    /*
    public void createGraph(Graph g) {
        graph = g;
    } */
 
    /*
     * default to simple TinkerGraph
     */    
    /*public void createGraph() {       
        graph = new TinkerGraph();
    } */   
    
    /*
     * shutting down Graph instance should flush all commits to disk
     */
    public void shutdown() throws ProxStorGraphDatabaseNotRunningException {
        isRunningOrException();        
        if (graph instanceof TransactionalGraph) {
            ProxStorDebug.println("ProxStorGraph.commit(): detected TransactionalGraph. committing.");
            ((TransactionalGraph) graph).commit();
        }
        graph.shutdown();
        graph = null;    
    } 
    
    public Graph getGraph() throws ProxStorGraphDatabaseNotRunningException {        
        isRunningOrException();
        return instance.graph;
    }    
    
    /*
     * "running" is whether non-null Graph instance exists
     */
    public boolean isRunning() {        
        return (graph != null);
    }
    
    private void isRunningOrException() throws ProxStorGraphDatabaseNotRunningException {
        if (!isRunning()) {
            throw new ProxStorGraphDatabaseNotRunningException("No running graph instance");
        }
    }
    
    public Vertex newVertex() throws ProxStorGraphDatabaseNotRunningException {
        isRunningOrException();
        return graph.addVertex(null);
    }
    
    public Edge newEdge(Vertex outVertex, Vertex inVertex, String label) throws ProxStorGraphDatabaseNotRunningException {
        isRunningOrException();
        return graph.addEdge(null, outVertex, inVertex, label);
    }
    
    public Vertex getVertex(Object id) throws ProxStorGraphDatabaseNotRunningException {
        isRunningOrException();
        return graph.getVertex(id);
    }
    
    public void commit() throws ProxStorGraphDatabaseNotRunningException {
        isRunningOrException();
        if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).commit();
            }
    }
            
    @Override
    public String toString() {
        try {
            isRunningOrException();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(ProxStorGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        return graph.toString();
    }
    
}
