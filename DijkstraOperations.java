import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Makes use of some classes I found that implement Dijkstra's pathfinding algorithm
 * to create a network for the 5x5 grid in the Bot's memory
 * in order to move towards the human player efficiently
 * and avoid walls in the process.
 * 
 * @author Jiri Swen
 * @version 1.0
 * @release 15/12/2017
 * @see {@link DijkstraAlgorithm.java}
 * @see {@link Vertex.java}
 * @see {@link Edge.java}
 * @see {@link Graph.java}
 * @see {@link Bot.java}
 */
public class DijkstraOperations {

	// The class that computes it all using a graph
	private DijkstraAlgorithm dijkstra;
	// The nodes of the network
	private List<Vertex> nodes;
	// All the edges in the network
	private List<Edge> edges;
	// The graph containing the nodes and the edges linking them
	private Graph graph;

/*
 _____        _  _    _         _  _              _    _               
|_   _|      (_)| |  (_)       | |(_)            | |  (_)              
  | |  _ __   _ | |_  _   __ _ | | _  ___   __ _ | |_  _   ___   _ __  
  | | | '_ \ | || __|| | / _` || || |/ __| / _` || __|| | / _ \ | '_ \ 
 _| |_| | | || || |_ | || (_| || || |\__ \| (_| || |_ | || (_) || | | |
 \___/|_| |_||_| \__||_| \__,_||_||_||___/ \__,_| \__||_| \___/ |_| |_|
 */

	/**
	 * Default constructor.
	 */
	public DijkstraOperations() {
		initialiseDijkstra();
	}

	/**
	 * Creates all the things necessary to run Dijkstra's algorithm.
	 * Got this method from vogella:
	 * https://github.com/vogellacompany/codeexamples-java/blob/master/de.vogella.algorithms.dijkstra/src/de/vogella/algorithms/dijkstra/test/TestDijkstraAlgorithm.java
	 */
	protected void initialiseDijkstra() {
		nodes = new ArrayList<Vertex>();

		// Creates the 25 nodes of the network
		for (int i = 0; i < 25; i++) {
			// Makes the ID of the vertices just a number for easy conversion
			Vertex location = new Vertex("" + i, "Node_" + i);
			nodes.add(location);
		}

		resetEdges();

		graph = new Graph(nodes, edges);
		dijkstra = new DijkstraAlgorithm(graph);
	}

	/**
	 * Creates all the edges (connections between nodes) of the network anew
	 * so that all have the same weight.
	 * <p>
	 * The edges are ordered like this:
	 * IDs 0-39:  horizontal edges (and e.g. 0-7 for the top row)
	 * IDs 40-79: vertical edges (and e.g. 40-47 for the leftmost column)
	 */
	protected void resetEdges() {
		edges = new ArrayList<Edge>();
		int edgeIDNumber = 0;
		/* Please look at the ordering as described above to understand the maths here: 
		   an Edge is always created between two node ID numbers and there are 5 nodes in a row */

		// Creates all horizontal edges
		for (int i = 0; i < 25; i += 5) {
			for (int j = 0; j < 4; ++j) {
				/* Edges are created back and forth because Dijkstra's algorithm uses
				   a directional graph and I want all connections to be bi-directional */
				addEdge("Edge_" + edgeIDNumber, i + j, i + j + 1);
				edgeIDNumber ++;
				addEdge("Edge_" + edgeIDNumber, i + j + 1, i + j);
				edgeIDNumber ++;
			}
		}

		// Creates all vertical edges
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 20; j += 5) {
				addEdge("Edge_" + edgeIDNumber, i + j, i + j + 5);
				edgeIDNumber ++;
				addEdge("Edge_" + edgeIDNumber, i + j + 5, i + j);
				edgeIDNumber ++;
			}
		}
	}

	/**
	 * Add and edge with weight 1 to the network.
	 * @param edgeID : The "name" for the new edge.
	 * @param sourceLocNo : The node from which the edge comes.
	 * @param destLocNo : The node to which the edge goes.
	 */
	protected void addEdge(String edgeID, int sourceLocNo, int destLocNo) {
		Edge newEdge = new Edge(edgeID, nodes.get(sourceLocNo), nodes.get(destLocNo), 1);
		edges.add(newEdge);
	}

/*
 _   _             _         _                           _  _      
| | | |           | |       | |                         | || |     
| | | | _ __    __| |  __ _ | |_  ___   __      __ __ _ | || | ___ 
| | | || '_ \  / _` | / _` || __|/ _ \  \ \ /\ / // _` || || |/ __|
| |_| || |_) || (_| || (_| || |_|  __/   \ V  V /| (_| || || |\__ \
 \___/ | .__/  \__,_| \__,_| \__|\___|    \_/\_/  \__,_||_||_||___/
       | |                                                         
       |_|                                                         
 */

	/**
	 * Creates the network anew in order to stay up-to-date on where the walls (i.e. obstacles) are.
	 * 
	 * @param List of the coordinates of all the wall tiles in the 5x5 grid. Called from {@link Bot}
	 */
	protected void updateNetwork(List<Integer[]> wallList) {
		resetEdges();
		for (int i = 0; i < wallList.size(); ++i) {
			addObstacle(wallList.get(i));
		}
		graph = new Graph(nodes, edges);
		dijkstra = new DijkstraAlgorithm(graph);
	}

	/**
	 * Adds an 'obstacle' in the network by setting the weights of all the edges around it to a high value.
	 * 
	 * @param The coordinates of the obstacle to add.
	 */
	protected void addObstacle(Integer[] coordinates) {
		List<Integer> edgeIDs = getEdgeIDsAroundPoint(coordinates);
		for (int i = 0; i < edgeIDs.size(); ++i) {
			Edge edgeToRemove = edges.get(edgeIDs.get(i));
			edgeToRemove.setWeight(99999);
			edges.set(edgeIDs.get(i), edgeToRemove);
		}
	}

	/**
	 * Gets the ID values of the edges around a node in the network.
	 * @param Coordinates of the point around which to take the edges.
	 * @return A list of the ID values of the wanted edges.
	 */
	protected List<Integer> getEdgeIDsAroundPoint (Integer[] coordinates) {
		List<Integer> edgeIDList = new ArrayList<Integer>();
		// Add the horizontal edges to the list
		edgeIDList.addAll(addEdgesToList(coordinates[0], coordinates[1], 0));
		// Add the vertical edges to the list
		edgeIDList.addAll(addEdgesToList(coordinates[1], coordinates[0], 40));
		return edgeIDList;
	}

	/**
	 * Gives the edges that lie around a point in either horizontal or vertical direction.
	 * For the vertical values, the coordinates are swapped and the offset is increased to 40:
	 * it is this easy because of the order in which the edges where constructed.
	 * <p>
	 * The math:
	 * If you think of the network as having 20 edges, you can do coordinate1 * 4 + coordinate2
	 * to land at the edge number at the right (/ top) of the point [1,2] (/ [2,1])
	 * (e.g. the point[3,1] gives edge 13, while edge 12 and 13 surround it).
	 * So then, you want to get the edges around it, you take this number -1 and the number itself.
	 * And of course, if it is at an edge of the network, the left/right side would need to be excluded.
	 * The same logic has been used, except all times 2.
	 * 
	 * @param coordinate1 : The first coordinate (either horizontal or vertical).
	 * @param coordinate2 : The second coordinate (either vertical or horizontal).
	 * @param offset : The edge ID number at which to start 'counting'.
	 * @return List of the horizontal/vertical edges adjacent to the tile.
	 */
	protected List<Integer> addEdgesToList(Integer coordinate1, Integer coordinate2, int offset) {
		List<Integer> halfEdgeIDList = new ArrayList<Integer>();
		int startI;
		int endI;
		if (coordinate2 == 0) {
			startI = 0;
		}
		else {
			startI = -2;
		}
		if (coordinate2 == 0 || coordinate2 == 4) {
			endI = startI + 2;
		}
		else {
			endI = startI + 4;
		}

		for (int i = startI; i < endI; ++i) {
			halfEdgeIDList.add(offset + coordinate1 * 8 + coordinate2 * 2 + i);
		}
		return halfEdgeIDList;
	}

/*
 _____        _                    _    _     
|  __ \      | |                  | |  | |    
| |  \/  ___ | |_    _ __    __ _ | |_ | |__  
| | __  / _ \| __|  | '_ \  / _` || __|| '_ \ 
| |_\ \|  __/| |_   | |_) || (_| || |_ | | | |
 \____/ \___| \__|  | .__/  \__,_| \__||_| |_|
                    | |                       
                    |_|                       
*/

	/**
	 * Run Dijkstra's algorithm (from {@link GameLogic}) based from a specified source tile.
	 * 
	 * @param Coordinates of the tile from which to run Dijkstra.
	 */
	protected void executeDijkstra(int[] mapCoordinates) {
		dijkstra.execute(nodes.get(mapCoordinatesToNodeID(mapCoordinates)));
	}

	/**
	 * Get the most efficient path to a specified tile from the source tile 
	 * that was used when running executeDijkstra(). Run from {@link GameLogic}.
	 * 
	 * @param Coordinates of the tile to calculate the path towards.
	 * @return A list of the vertices (nodes) to the target tile, starting at the source tile.
	 */
	protected LinkedList<Vertex> getShortestPath(int[] mapCoordinates) {
		return dijkstra.getPath(nodes.get(mapCoordinatesToNodeID(mapCoordinates)));
	}

/*
  ___                _  _  _                     
 / _ \              (_)| |(_)                    
/ /_\ \ _   _ __  __ _ | | _   __ _  _ __  _   _ 
|  _  || | | |\ \/ /| || || | / _` || '__|| | | |
| | | || |_| | >  < | || || || (_| || |   | |_| |
\_| |_/ \__,_|/_/\_\|_||_||_| \__,_||_|    \__, |
                                            __/ |
                                           |___/ 
 */

  /**
   * @param Coordinates to convert.
   * @return The corresponding Node ID.
   */
	protected static int mapCoordinatesToNodeID(int[] coordinates) {
		// (Remember, there are 5 nodes in each row)
		return coordinates[0] * 5 + coordinates[1];
	}

	/**
	 * @param Node ID to convert.
	 * @return The corresponding set of coordinates.
	 */
	protected static int[] nodeIDToMapCoordinates(int nodeID) {
		int[] coordinates = new int[] {nodeID / 5, nodeID % 5};
		return coordinates;
	}

	/**
	 * @param Node (vertex) to get the ID number from.
	 * @return The corresponding ID number.
	 */
	protected static int vertexToNodeID(Vertex node) {
		return (int) Integer.parseInt(node.getId());
	}
}