package com.giannoules.proxstor;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * hold ProxStor instance-wide reference to Graph and provide internal interface
 * to actual back-end graph relational database
 * 
 * intercepts:
 *   - vertex creation, update, retrieval
 *   - edge creation, update, retrieval
 *   - graph traversal
 * 
 * provides:
 *   - transactional related method independent of whether graph instance of TransactionalGraph
 * 
 * manages:
 *   - indexes
 */
public enum ProxStorGraph {
    instance;
    
    private Graph graph;
   
    /*
     * nada. use createGraph()
     */
    ProxStorGraph() { }
   
    /*
     * preferred use of GraphFactory
     */
    public void start(Map<String, String> conf) throws ProxStorGraphDatabaseAlreadyRunning {
        if (graph instanceof Graph) {
            throw new ProxStorGraphDatabaseAlreadyRunning("cannot create graph because an instance is already running");
        }
        graph = GraphFactory.open(conf);
    }     
     
    /*
     * shutting down Graph instance should flush all commits to disk
     */
    public void shutdown() throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();        
        if (graph instanceof TransactionalGraph) {
            ProxStorDebug.println("ProxStorGraph.commit(): detected TransactionalGraph. committing.");
            ((TransactionalGraph) graph).commit();
        }
        graph.shutdown();
        graph = null;    
    } 
      
    /*
     * "running" is whether non-null Graph instance exists
     */
    public boolean isRunning() {        
        return (graph != null);
    }

    /*
     * add vertex to graph
     */
    public Vertex addVertex() throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();
        return graph.addVertex(null);
    }
    
    /*
     * add edge to graph
     */
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();
        return graph.addEdge(null, outVertex, inVertex, label);
    }
    
    /*
     * return Vertex referenced by id
     */
    public Vertex getVertex(Object id) throws ProxStorGraphDatabaseNotRunningException, ProxStorGraphNonExistentObjectID {
        _isRunningOrException();
        /* 
         * getVertex() returns null if no Vertex exists (no Exception from Blueprints)
         * in this case throw our own exception
         */
        Vertex v = graph.getVertex(id);
        if (v == null)
            throw new ProxStorGraphNonExistentObjectID("Invalid Vertex ID: " + id.toString());
        return v;
    }
    
    /*
     * return vertices by key:value
     *
     * @TODO deprecate with index interface
     */
    public Iterable<Vertex> getVertices(String key, Object value) throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();
        return graph.getVertices(key, value);
    }
    
    /*
     * provide query interface for special purposes
     *
     * @TODO hide this behind an interface
     */
    public GraphQuery _query() throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();
        return graph.query();
    }
    
    /*
     * return Edge referenced by id
     */
    public Edge getEdge(Object id) throws ProxStorGraphDatabaseNotRunningException, ProxStorGraphNonExistentObjectID {
        _isRunningOrException();
        /* 
         * getEdge() returns null if no Edege exists (no Exception from Blueprints)
         * in this case throw our own exception
         */
        Edge e = graph.getEdge(id);
        if (e == null)
            throw new ProxStorGraphNonExistentObjectID("Invalid Edge ID: " + id.toString());
        return e;
    }
    
    /*
     * if transactional graph, commit.
     * if not, do nothing.    
     * 
     * no status, no return.
     */
    public void commit() throws ProxStorGraphDatabaseNotRunningException {
        _isRunningOrException();
        if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).commit();
            }
    } 
    
    /*
     * return the .toString() of the Graph instance combined with Features
     */
    @Override
    public String toString() {
        try {
            _isRunningOrException();
        } catch (ProxStorGraphDatabaseNotRunningException ex) {
            Logger.getLogger(ProxStorGraph.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(graph.toString());
        sb.append("\n\nFeatures:\n\n");
        sb.append(graph.getFeatures());
        return sb.toString();    
    } 
    
    /*
     * internal method to wrap checking for running graph, and if not, throw exception
     */
    private void _isRunningOrException() throws ProxStorGraphDatabaseNotRunningException {
        if (!isRunning()) {
            throw new ProxStorGraphDatabaseNotRunningException("No running graph instance");
        }
    }
    
    /*
     * @TODO get away from providing this...
     */
    @Deprecated
    private Graph _getGraph() throws ProxStorGraphDatabaseNotRunningException {        
        _isRunningOrException();
        return instance.graph;
    }    
    
}