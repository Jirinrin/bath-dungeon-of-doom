/**
 * A directional edge/connection of a network/graph.
 * 
 * @author vogella: released under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Germany
 * https://github.com/vogellacompany/codeexamples-java/blob/master/de.vogella.algorithms.dijkstra/src/de/vogella/algorithms/dijkstra/model/Edge.java
 * @release 30/08/2010
 * @see {@link DijkstraOperations.java}
 * @see {@link DijkstraAlgorithm.java}
 * @see {@link Vertex.java}
 * @see {@link Graph.java}
 */
public class Edge  { ////from http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html#copyright-and-license
    private final String id;
    private final Vertex source;
    private final Vertex destination;
    private int weight;

    public Edge(String id, Vertex source, Vertex destination, int weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }
    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }

    public void setWeight(int newWeight) { // Added by me
    		this.weight = newWeight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }


}