import java.util.List;

/**
 * A graph containing nodes and directional edges linking them
 * 
 * @author vogella: released under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Germany
 * https://github.com/vogellacompany/codeexamples-java/blob/master/de.vogella.algorithms.dijkstra/src/de/vogella/algorithms/dijkstra/model/Graph.java
 * @release 30/08/2010
 * @see {@link DijkstraAlgorithm.java}
 * @see {@link DijkstraOperations.java}
 * @see {@link Vertex.java}
 * @see {@link Edge.java}
 */
public class Graph { ////from http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html#copyright-and-license
    private final List<Vertex> vertexes;
    private final List<Edge> edges;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }



}