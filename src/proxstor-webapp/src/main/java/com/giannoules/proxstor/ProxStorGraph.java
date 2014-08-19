/*
 * Copyright (c) 2014, Jim Giannoules
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.giannoules.proxstor;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphFactory;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import java.util.Map;

/**
 *
 * @author jim
 * 
 *  hold ProxStor instance-wide reference to Graph
 * 
 */
public enum ProxStorGraph {
    instance;
    
    private Graph graph;
    
    ProxStorGraph() { }
   
    public void createGraph(Map<String, String> conf) {
        graph = GraphFactory.open(conf);
    }
   
    public void createGraph(Graph g) {
        graph = g;
    }
    
    public void createGraph() {
        graph = new TinkerGraph();
    }
    
    public void shutdown() {
        if (graph != null) {
            graph.shutdown();
            graph = null;
        }
    }
    
    public ProxStorGraph getInstance() {
        return instance;
    }
    
    public Graph getGraph() {
        return instance.graph;
    }
    
    public boolean isRunning() {
        return (graph != null);
    }
    
    public Vertex getNewVertex() {
        return graph.addVertex(null);
    }   
    
    @Override
    public String toString() {
        return graph.toString();
    }
    
}
