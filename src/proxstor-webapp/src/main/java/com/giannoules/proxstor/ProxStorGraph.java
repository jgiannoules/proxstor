package com.giannoules.proxstor;

import com.giannoules.proxstor.exception.ProxStorGraphDatabaseAlreadyRunning;
import com.giannoules.proxstor.exception.ProxStorGraphDatabaseNotRunningException;
import com.giannoules.proxstor.exception.ProxStorGraphNonExistentObjectID;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
 * 
 * @author Jim Giannoules
 */
public enum ProxStorGraph {
    instance;
    
    // Graph instance held and managed by this class
    public Graph graph;
    
    // stats & statsPrev used to track the method call count emitted in toString()
    private final Map<String, AtomicInteger> stats = new ConcurrentHashMap<>();
    private final Map<String, Integer> statsPrev = new ConcurrentHashMap<>();
        
    /**
     * Do nothing constructor. use createGraph()
     */
    ProxStorGraph() { }
   
    /**
     * This is the preferred method to create a new Graph connection instance. Uses
     * a GraphFactory to allow connection to any one of a multitude of back-end 
     * graph relational databases.
     * 
     * @param conf Configuration Map to be passed to GraphFactory
     * @throws com.giannoules.proxstor.exception.ProxStorGraphDatabaseAlreadyRunning If a connection to a database already exists.
     */
    public void start(Map<String, String> conf) throws ProxStorGraphDatabaseAlreadyRunning {
        incCounter("start()");
        if (graph instanceof Graph) {
            throw new ProxStorGraphDatabaseAlreadyRunning("cannot create graph because an instance is already running");
        }
        graph = GraphFactory.open(conf);
        if (graph instanceof KeyIndexableGraph) {
            ProxStorDebug.println("KeyIndexableGraph detected.");
            try {
                ((KeyIndexableGraph) graph).createKeyIndex("_type", Vertex.class);
                ((KeyIndexableGraph) graph).createKeyIndex("latitude", Vertex.class);
                ((KeyIndexableGraph) graph).createKeyIndex("longitude", Vertex.class);
            } catch (Exception ex) { }
        }
        if (graph instanceof OrientGraph) {
                ((OrientGraph) graph).createEdgeType("knows");
                ((OrientGraph) graph).createEdgeType("previously_at");
                ((OrientGraph) graph).createEdgeType("currently_at");
                ((OrientGraph) graph).createEdgeType("uses");
                ((OrientGraph) graph).createEdgeType("contains");
                ((OrientGraph) graph).createEdgeType("nearby");
                ((OrientGraph) graph).createEdgeType("within");
        }
    }     
     
    /**
     * Shut down a running database connection. Depending on the configuration used to
     * create the connection, shutting down might commits to disk a consistent copy
     * of the database.
     * 
     * @throws ProxStorGraphDatabaseNotRunningException If a database connection was not already established.
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

    /**
     * Tests whether a database connection is already established.
     * 
     * @return true if database connection established; false otherwise
     */
    public boolean isRunning() {
        incCounter("isRunning()");        
        return (graph != null);
    }

    /**
     * Adds a Vertex to the connected Graph instance.
     * 
     * @return Newly created Vertex instance.
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    public Vertex addVertex() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("addVertex()");        
        _isRunningOrException();
        return graph.addVertex(null);
    }
    
    /**
     * Adds an Edge with label between two existing vertices. Edges are directed, 
     * so one vertex is 'in' while another is 'out'.
     * 
     * (outVertex) ----[label]----> (inVertex)
     * 
     * @param outVertex Vertex with out relationship to the Edge 
     * @param inVertex  Vertex with the in relationship to the Edge
     * @param label     String to use as a label to this Edge
     * @return          Newly created Edge
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    public Edge addEdge(Vertex outVertex, Vertex inVertex, String label) throws ProxStorGraphDatabaseNotRunningException {
        incCounter("addEdge()");
        _isRunningOrException();
        return graph.addEdge(null, outVertex, inVertex, label);
    }
 
    /**
     * Returns Vertex referenced by provided identifier.
     * 
     * @param id    Identifier for the Vertex to retrieve.
     * @return      Vertex with id
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     * @throws ProxStorGraphNonExistentObjectID If the provided identifier does not reference a valid Vertex.
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

    /**
     * Return Vertices which have a given key:value pair. The Graph is searched
     * globally for all matches.
     * 
     * @param key   Key of the key:value pair
     * @param value Value of the key:value pair
     * @return      Iterable containing all matching Vertex instances
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    public Iterable<Vertex> getVertices(String key, Object value) throws ProxStorGraphDatabaseNotRunningException {
        incCounter("getVertices()");
        _isRunningOrException();
        return graph.getVertices(key, value);
    }
    
    /**
     * Return an instance of GraphQuery for the current Graph connection. A
     * GraphQuery is typically used to execute more fine-grained searching of
     * database objects.
     * 
     * @return GraphQuery instance.
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    public GraphQuery _query() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("_query()");
        _isRunningOrException();
        return graph.query();
    }
    
    /**
     * Returns Edge referenced by provided identifier.
     * 
     * @param id    Identifier for the Edge to retrieve.
     * @return      Edge with id.
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     * @throws ProxStorGraphNonExistentObjectID If the provided identifier does not reference a valid Edge.
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
    
    /**
     * For pending transactions to be committed to the database. If the graph
     * instance is not transactional this method simply exits.
     * 
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    public void commit() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("commit()");
        _isRunningOrException();
        if (graph instanceof TransactionalGraph) {
                incCounter("TransactionalGraph commit()");
                ((TransactionalGraph) graph).commit();
        }
    } 
   
    /**
     * Rollback outstanding transactions from the database. If the graph
     * instance is not transactional this method simply exits.
     * 
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */   
    public void rollback() throws ProxStorGraphDatabaseNotRunningException {
        incCounter("rollback()");
        _isRunningOrException();
        if (graph instanceof TransactionalGraph) {
                incCounter("TransactionalGraph rollback()");
                ((TransactionalGraph) graph).rollback();
        }
    }
    
    /**
     * Return String representation of Graph database connection. Typically
     * includes features of the Graph. Additionally the count of number of
     * ProxStorGraph methods called since last toString() is included.
     * 
     * @return String representation.
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
    
    
    // ------------> PRIVATE METHODS BELOW <--------------
    
  
    /**
     * Internal helper method checking for running graph and throw exception.
     * 
     * @throws ProxStorGraphDatabaseNotRunningException If database connection not already established.
     */
    private void _isRunningOrException() throws ProxStorGraphDatabaseNotRunningException {
        if (!isRunning()) {
            throw new ProxStorGraphDatabaseNotRunningException("No running graph instance");
        }
    }
   
    /**
     * For the given description parameter increment the respective counter. This
     * method is used internally by ProxStorGraph to track usage statistics of
     * individual methods.
     * 
     * @param desc Description String to used a reference to counter
     * @return Updated counter for the description.
     */
    private int incCounter(String desc) {
        if (stats.containsKey(desc)) {
            return stats.get(desc).incrementAndGet();
        } else {
            stats.put(desc, new AtomicInteger(1));
            return 1;
        }
    }

}
