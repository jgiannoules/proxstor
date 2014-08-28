package com.giannoules.proxstor;

import com.giannoules.proxstor.exception.ProxStorGraphDatabaseAlreadyRunning;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Map<String, AtomicInteger> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> statsPrev = new ConcurrentHashMap<>();
   
    private int incCounter(String desc) {
        if (stats.containsKey(desc)) {
            return stats.get(desc).incrementAndGet();
        } else {
            stats.put(desc, new AtomicInteger(1));
            return 1;
        }
    }
    
    /*
     * nada. use createGraph()
     */
    ProxStorGraph() { }
   
    /*
     * preferred use of GraphFactory
     */
    public void start(Map<String, String> conf) throws ProxStorGraphDatabaseAlreadyRunning {
        incCounter("start()");
        if (graph instanceof Graph) {
            throw new ProxStorGraphDatabaseAlreadyRunning("cannot create graph because an instance is already running");
        }
        graph = GraphFactory.open(conf);        
    }     
     
    /*
     * shutting down Graph instance should flush all commits to disk
     */
    public void shutdown() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("shutdown()");
        _isRunningOrException();        
        if (graph instanceof TransactionalGraph) {
            incCounter("shutdown() - TransactionalGraph");
            ((TransactionalGraph) graph).commit();
        }
        graph.shutdown();
        graph = null;    
    } 
      
    /*
     * "running" is whether non-null Graph instance exists
     */
    public boolean isRunning() {
        incCounter("isRunning()");        
        return (graph != null);
    }

    /*
     * add vertex to graph
     */
    public Vertex addVertex() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("addVertex()");        
        _isRunningOrException();
        return graph.addVertex(null);
    }
    
    /*
     * add edge to graph
     */
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws ProxStorGraphDatabaseNotRunningException {
        incCounter("addEdge()");
        _isRunningOrException();
        return graph.addEdge(null, outVertex, inVertex, label);
    }
    
    /*
     * return Vertex referenced by id
     */
    public Vertex getVertex(Object id) throws ProxStorGraphDatabaseNotRunningException, ProxStorGraphNonExistentObjectID {
        incCounter("getVertex()");
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
        incCounter("getVertices()");
        _isRunningOrException();
        return graph.getVertices(key, value);
    }
    
    /*
     * provide query interface for special purposes
     *
     * @TODO hide this behind an interface
     */
    public GraphQuery _query() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("_query()");
        _isRunningOrException();
        return graph.query();
    }
    
    /*
     * return Edge referenced by id
     */
    public Edge getEdge(Object id) throws ProxStorGraphDatabaseNotRunningException, ProxStorGraphNonExistentObjectID {
        incCounter("getEdge()");
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
    
    public List<Vertex> getVertices(String idA, String idB, Direction d, String ... labels) throws ProxStorGraphDatabaseNotRunningException, ProxStorGraphNonExistentObjectID {
        incCounter("getVertices()");
        Vertex a = getVertex(idA);
        List<Vertex> vertices = new ArrayList<>();
        for (Vertex v : a.getVertices(d, labels)) {
            if (idB.equals((String) v.getId())) {
                vertices.add(v);
            }
        }
        return vertices;
    }      
    
    /*
     * if transactional graph, commit.
     * if not, do nothing.    
     * 
     * no status, no return.
     */
    public void commit() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("commit()");
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
        incCounter("toString()");
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
        sb.append("\n\nProxStorGraph Internal Stats:\n\n");
        for (String k : stats.keySet()) {
            sb.append(k).append(": ").append(stats.get(k));
            if (statsPrev.containsKey(k)) {
                sb.append(" (+");
                sb.append(stats.get(k).get() - statsPrev.get(k));
                sb.append(")");
            }            
            sb.append("\n");
            statsPrev.put(k, stats.get(k).get());
        }
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