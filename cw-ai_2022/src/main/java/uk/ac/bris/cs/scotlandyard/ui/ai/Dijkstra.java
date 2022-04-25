package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.function.Predicate;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport.FERRY;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.getPlayerTickets;

public class Dijkstra{ //something we can give minimaxbox to score a game state

    Dictionary<Integer, ArrayList<Integer>> nodeDict;

    public Dijkstra() {
        nodeDict = new Hashtable<Integer, ArrayList<Integer>>(0);
    }
    //build the datastructures holding the nodes, their distance from source and their preceding node
    private void populate(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, Integer source){
        //when a game is reset, this should refresh the hashtable (bug checking)
        if (!nodeDict.isEmpty()) {
            //System.out.println("New Game!");
            nodeDict = new Hashtable<Integer, ArrayList<Integer>>(0);
        }
        nodeDict.put(source, new ArrayList<Integer>(Arrays.asList(0, null)));
        for(Integer n : graph.nodes()){
            if( !n.equals(source)) {
                nodeDict.put(n, new ArrayList<Integer>(Arrays.asList(Integer.MAX_VALUE, null)));
            }
        }
    }

    public Dictionary<Integer, ArrayList<Integer>> getPopulatedDict() {
        return nodeDict;
    }

    //checks if a list contains a particular node
    private boolean searchList(Integer[] nodes, Integer x) {
        return Arrays.asList(nodes).contains(x);
    }

    //pass in the detective so that we can track their tickets
    public NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> shortestPathFromSourceToDestination(
            Integer destination,
            Player detective,
            Board.GameState board) {

        //set up needed variables
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = board.getSetup().graph;
        Integer source = detective.location();

        //throws necessary exceptions to ensure correctness, source and destination must be within the graph
        if (!graph.nodes().contains(source)) throw new IllegalArgumentException("Detective not on graph!");
        if (!graph.nodes().contains(destination)) throw new IllegalArgumentException("Mr X not on graph!");

        //create a mutable copy of the player tickets to test if they can even move to certain nodes
        Map<ScotlandYard.Ticket, Integer> ticketsCpy = new HashMap<>(Map.copyOf(getPlayerTickets(board, detective.piece())));
        List<ScotlandYard.Ticket> ticketsUsed = new ArrayList<ScotlandYard.Ticket>();
        populate(graph, source);

        int pos = 0;
        Integer[] visitedNodes = new Integer[200];
        Integer currentNode = source;
        ArrayList<Integer> endPoints = new ArrayList<>(); //places we know at least one distance to (we've calculated at least once)
        endPoints.add(source);

        //the algorithm goes through the whole graph until it reaches the destination node
        while (!currentNode.equals(destination)) {
            //getting successors
            Object[] succ = graph.successors(currentNode).toArray();
            for (Object node : succ) {
                if(graph.edgeValue((Integer) node, currentNode).isPresent() && !searchList(visitedNodes, (Integer)node) && !(node.equals(source))) {
                    //each edge value is worth one
                    ScotlandYard.Ticket ticketNeeded = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0).requiredTicket();
                    Integer distance = 0;
                    //detectives cannot travel by ferry, thus we should ignore the paths that contain a ferry journey
                    boolean needsFerry = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0) == FERRY;
                    if (!needsFerry) {
                        distance = nodeDict.get(currentNode).get(0) + 1;
                        //System.out.println("Detective: " + detective + " Ticket Needed: " + ticketNeeded + " Node: " + node);
                        //only update the distance if its shorter than our shortest to that node
                        if(distance < nodeDict.get(node).get(0)) { nodeDict.put((Integer) node, new ArrayList<Integer>(Arrays.asList(distance, currentNode))); }

                        //all endpoints can be distinct
                        if(!endPoints.contains((Integer) node)) { endPoints.add((Integer) node); } //add this node to the endpoints list
                    }
                }
            }


            Integer p = nodeDict.get(currentNode).get(1);
            if (p != null && graph.edgeValue(p, currentNode).isPresent()) {
                visitedNodes[pos] = currentNode;
                pos++;

            }

            endPoints.removeIf(Predicate.isEqual(currentNode)); //removes current node (its been fully visited)
            Integer localBestDist = Integer.MAX_VALUE; Integer d;
            for(Integer node : endPoints){// selects the shortest distance of all endpoints bar the ones already visited
                d = nodeDict.get(node).get(0);
                if(d < localBestDist){
                    currentNode = node;
                    localBestDist = d;
                }
            }

        }

        //builds the shortest path
        List<Integer> path = new ArrayList<Integer>();
        path.add(0, currentNode);
        while (!currentNode.equals(source)) {
            if (nodeDict.get(currentNode).get(1) == null) System.out.println("Source: " + source + " Destination: " + destination + " Current Node: " + currentNode);
            currentNode = nodeDict.get(currentNode).get(1);

            path.add(0, currentNode);
        }

        for (int i = 0; i < path.size()-1; i++) {
            if (graph.edgeValue(path.get(i), path.get(i+1)).isPresent() &&  !(graph.edgeValue(path.get(i), path.get(i+1)).isEmpty())) {
                ScotlandYard.Ticket transportTaken = graph.edgeValue(path.get(i), path.get(i+1)).get().stream().toList().get(0).requiredTicket();
                ticketsUsed.add(transportTaken);
            }
        }

        return new NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>>(nodeDict.get(destination).get(0), path, ticketsUsed);//needs to include source
    }
}
