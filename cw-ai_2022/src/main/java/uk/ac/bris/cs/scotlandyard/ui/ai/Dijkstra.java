package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.min;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport.FERRY;

public class Dijkstra{ //something we can give minimaxbox to score a game state

    private void populate(Dictionary<Integer, ArrayList<Integer>> nodeDict,
                                                            ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, Integer source){
        nodeDict.put(source, new ArrayList<Integer>(Arrays.asList(0, null)));
        for(Integer n : graph.nodes()){
            if( !n.equals(source)) {
                nodeDict.put(n, new ArrayList<Integer>(Arrays.asList(Integer.MAX_VALUE, null)));
            }
        }
    }

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

        //Track tickets
        while (!currentNode.equals(destination)) {
            //getting successors
            Object[] succ = graph.successors(currentNode).toArray();
            for (Object node : succ) {
                if(graph.edgeValue((Integer) node, currentNode).isPresent() && !searchList(visitedNodes, (Integer)node) && !(node.equals(source))) {
                    //each edge value is worth one (working version)
                    ScotlandYard.Ticket ticketNeeded = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0).requiredTicket();
                    Integer distance = 0;
//					!needsFerry && ticketsCpy.get(ticketNeeded) > 0
                    boolean needsFerry = graph.edgeValue((Integer) node, currentNode).get().stream().toList().get(0) == FERRY;
                    if (!needsFerry) {
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

    //reconstructing the player tickets
    @Nonnull
    ImmutableMap<ScotlandYard.Ticket, Integer> getPlayerTickets(Board.GameState board, Piece piece) {
        ArrayList<ScotlandYard.Ticket> ticketTypes = new ArrayList<ScotlandYard.Ticket>(Arrays.asList(TAXI, BUS, UNDERGROUND));

        if (piece.isMrX()) ticketTypes.addAll(Arrays.asList(DOUBLE, SECRET));
        Map<ScotlandYard.Ticket, Integer> tickets = new HashMap<ScotlandYard.Ticket, Integer>();

        for (ScotlandYard.Ticket ticket : ticketTypes) {
            if (board.getPlayerTickets(piece).isPresent()) {
                tickets.put(ticket, board.getPlayerTickets(piece).get().getCount(ticket));
            }
        }

        return ImmutableMap.copyOf(tickets);
    }
    @Nonnull
    private List<Player> getPlayers(Board.GameState board) {
        List<Piece.Detective> detectives = board.getPlayers().stream().filter(Piece::isDetective).map(y -> (Piece.Detective)y).toList();
        List<Piece.MrX> mrXSingle = board.getPlayers().stream().filter(Piece::isMrX).map(y -> (Piece.MrX)y).limit(1).toList();

        List<Piece> pieces = new ArrayList<Piece>();
        pieces.add(mrXSingle.get(0));
        pieces.addAll(detectives);

        List<Player> players = new ArrayList<Player>();

        for (Piece piece : pieces) {
            if (piece.isDetective()) {
                boolean hasLocation = board.getDetectiveLocation((Piece.Detective)piece).isPresent();
                if (hasLocation) {
                    int location = board.getDetectiveLocation((Piece.Detective)piece).get();
                    Player newDetective = new Player(piece, getPlayerTickets(board, piece),location);
                    players.add(newDetective);
                }

            }
            else {
                ImmutableList<LogEntry> log = board.getMrXTravelLog();
                int n = log.size();
                boolean mrXRound = board.getAvailableMoves().stream().anyMatch(x -> x.commencedBy() == Piece.MrX.MRX);
                //default location (no significance)
                int location = 1;
                if (n > 0) {
                    LogEntry lastLog = log.get(n-1);
                    boolean hasLocation = lastLog.location().isPresent();
                    if (hasLocation) {
                        location = lastLog.location().get();
                    }
                } else if (mrXRound) {
                    List<Move> grabMoves = board.getAvailableMoves().stream().limit(1).toList();
                    Move grabMove = grabMoves.get(0);
                    location = grabMove.source();
                }

                Player newMrX = new Player(piece, getPlayerTickets(board, piece), location);
                players.add(newMrX);


            }
        }

        return players;
    }

    private List<Player> getDetectives(Board.GameState board) {
        return getPlayers(board).stream().filter(Player::isDetective).toList();
    }

    private Player getMrX(Board.GameState board) {
        List<Player> mrXS = getPlayers(board).stream().filter(Player::isMrX).toList();
        return mrXS.get(0);
    }

    //we want to exclude values that are very far
    private Integer calcDistanceScore(List<Integer> distances) {
        //compute mean
        int totalSum = distances.stream().mapToInt(x -> x.intValue()).sum();
        int n = distances.size();
        int mean = Math.floorDiv(totalSum, n);
        int sumofSqr = 0;
        for (Integer distance : distances) {
            sumofSqr += (int)Math.pow((distance - mean), 2);
        }

        int sd = (int)Math.floorDiv(sumofSqr, (n-1)); //standard deviation

        Integer closestLocation = min(distances); //get distance of closest detective
        List<Integer> noOutlierDist = new ArrayList<>();
        noOutlierDist.add(closestLocation);
        distances.remove(closestLocation);

        //only consider statistically close distances (1sd)
        for (Integer distance : distances) {
            if (distance <= closestLocation + sd) noOutlierDist.add(distance);
        }

        //compute the mean of these values
        int goodSum = noOutlierDist.stream().mapToInt(x -> x.intValue()).sum();
        int goodN = noOutlierDist.size();



        return (int)Math.floorDiv(goodSum, goodN);
    }

    private int cumulativeDistance(Board.GameState board, Player mrX, List<Player> detectives) {
        Integer mrXLocation = mrX.location();
        List<Integer> distancePath = new ArrayList<>();
        for (Player detective : detectives) {
            Integer detectiveLocation = detective.location();
            var path = shortestPathFromSourceToDestination(board.getSetup().graph, detectiveLocation, mrXLocation, detective, board);
            int distance = path.getFirst();
            //System.out.println(distance);
            distancePath.add(distance);
            List<Integer> nodes = path.getMiddle(); //may want to use for whatever reason
            List<ScotlandYard.Ticket> ticketUsed = path.getLast(); //for testing, assert that detective had enough tickets to travel that path

        }

        //distancePath.sort((x, y) -> x - y);



        return calcDistanceScore(distancePath);
    }


}
