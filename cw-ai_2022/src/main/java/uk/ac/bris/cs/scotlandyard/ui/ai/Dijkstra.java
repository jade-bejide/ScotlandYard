package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.function.Predicate;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport.FERRY;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.*;

public class Dijkstra{ //something we can give minimaxbox to score a game state

    //build the datastructures holding the nodes, their distance from source and their preceding node
    private void populate(Dictionary<Integer, ArrayList<Integer>> nodeDict,
                                                            ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, Integer source){
        nodeDict.put(source, new ArrayList<Integer>(Arrays.asList(0, null)));
        for(Integer n : graph.nodes()){
            if( !n.equals(source)) {
                nodeDict.put(n, new ArrayList<Integer>(Arrays.asList(Integer.MAX_VALUE, null)));
            }
        }
    }

    //checks if a list contains a particular node
    private boolean searchList(Integer[] nodes, Integer x) {
        return Arrays.asList(nodes).contains(x);
    }

    //pass in the detective so that we can track their tickets
    public NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> shortestPathFromSourceToDestination(
            ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph,
            Integer source,
            Integer destination,
            Player detective,
            Board.GameState board) {
        //create a mutable copy of the player tickets to test if they can even move to certain nodes
        Map<ScotlandYard.Ticket, Integer> ticketsCpy = new HashMap<>(Map.copyOf(getPlayerTickets(board, detective.piece())));
        Dictionary<Integer, ArrayList<Integer>> nodeDict = new Hashtable<Integer, ArrayList<Integer>>();
        List<ScotlandYard.Ticket> ticketsUsed = new ArrayList<ScotlandYard.Ticket>();
        populate(nodeDict, graph, source);

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
                    //each edge value is worth one (working version)
                    ScotlandYard.Ticket ticketNeeded = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0).requiredTicket();
                    Integer distance = 0;
                    //detectives cannot travel by ferry, thus we should ignore the paths that contain a ferry journey
                    boolean needsFerry = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0) == FERRY;
                    if (!needsFerry) {
                        System.out.println(ticketNeeded);
                        distance = nodeDict.get(currentNode).get(0) + 1;
                        //System.out.println("Detective: " + detective + " Ticket Needed: " + ticketNeeded + " Node: " + node);
                        //only update the distance if its shorter than our shortest to that node
                        if(distance < nodeDict.get(node).get(0)) { nodeDict.put((Integer) node, new ArrayList<Integer>(Arrays.asList(distance, currentNode))); }
                        ticketsUsed.add(ticketNeeded);
                        //all endpoints can be distinct
                        if(!endPoints.contains((Integer) node)) { endPoints.add((Integer) node); } //add this node to the endpoints list
                    }
                }
            }


            Integer p = nodeDict.get(currentNode).get(1);
            if (p != null && graph.edgeValue(p, currentNode).isPresent()) {

                ScotlandYard.Ticket transportTaken = graph.edgeValue(p, currentNode).get().stream().toList().get(0).requiredTicket();
                visitedNodes[pos] = currentNode;
                pos++;
//                if (ticketsCpy.get(transportTaken) > 0) {
//
//
////					ticketsCpy.put(transportTaken, ticketsCpy.get(transportTaken) -1);
//                }

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

        List<Integer> path = new ArrayList<Integer>();
        path.add(0, currentNode);
        while (!Objects.equals(currentNode, source)) {
            currentNode = nodeDict.get(currentNode).get(1);
            path.add(0, currentNode);
        }

        return new NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>>(nodeDict.get(destination).get(0), path, ticketsUsed);//needs to include source
    }


}
