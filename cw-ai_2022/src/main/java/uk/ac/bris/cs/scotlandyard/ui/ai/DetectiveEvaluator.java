package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import java.util.*;
import java.util.stream.Collectors;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Transport.TAXI;

public class DetectiveEvaluator extends Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Double> weights;
    private Set<Integer> possibleMrXLocations = new HashSet<Integer>();

    public DetectiveEvaluator(List<Double> weights){
        this.weights = flatten(weights);
        //mr X boundary is initialised to the entire graph
        for(int i = 1; i < 200; i++) {
            this.possibleMrXLocations.add(i);
        }
    }

    //get and set boundaries

    private Set<Integer> filterBoundary(Board.GameState board, Set<Integer> boundary, Integer revealedLocation) {
        if (board.getPlayerTickets(Piece.MrX.MRX).isPresent()) {
            var mrXTickets = board.getPlayerTickets(Piece.MrX.MRX).get();
            boundary = boundary.stream().filter(x -> {
                ImmutableSet<ScotlandYard.Transport> neededTickets = board.getSetup().graph.edgeValueOrDefault(x, revealedLocation, ImmutableSet.of(TAXI));

                if (neededTickets != null) {
                    for (ScotlandYard.Transport transport : neededTickets) {
                        if (mrXTickets.getCount(transport.requiredTicket()) < 1) return false;
                    }
                }

                return true;

            }).collect(Collectors.toSet());
        }


        return boundary;
    }

    //refocuses mr X's boundary each time he reveals himself
    public void setMrXBoundary(Integer revealedLocation, Board.GameState board, boolean isFiltering) {
        if (revealedLocation < 1 || revealedLocation > 199) throw new IllegalArgumentException("Not a valid location");
        Set<Integer> boundary = new HashSet<Integer>(board.getSetup().graph.successors(revealedLocation));

        if (isFiltering) boundary = filterBoundary(board, boundary, revealedLocation);
        Set<Integer> boundaryCpy = Set.copyOf(boundary);
//        for (Integer node : boundaryCpy) {
//            boundary.addAll(board.getSetup().graph.successors(node));
//        }
        if (isFiltering) boundary = filterBoundary(board, boundary, revealedLocation);
        possibleMrXLocations = boundary;
    }

    //needed for testing
    public Set<Integer> getMrXBoundary() {
        return possibleMrXLocations;
    }

    //checks if Mr X's location has been revealed and update Mr X boundary as appropriate
    private void whenRevealed(Board.GameState board) {

        if (BoardHelper.getLastLog(board).location().isPresent()) setMrXBoundary(BoardHelper.getLastLog(board).location().get(), board, false);
    }

    public int getMrXLocation(Board.GameState board) {
        if (BoardHelper.getLastLog(board).location().isPresent()) return BoardHelper.getLastLog(board).location().get();
        //should never reach here
        return 1;
    }

    public boolean isRevealed(Board.GameState board) {
        return BoardHelper.getLastLog(board).location().isPresent();
    }

    //for static evaluation, calculate distance to mr X
    private int getDistanceToMrX(Piece inPlay, Board.GameState board){
        Random rand = new Random();
        List<Integer> possibleMrXLocationsList = new ArrayList<Integer>(possibleMrXLocations);
        //here they all decide that mr X is potentially at different locations rather than having a common goal
        Integer targetNode = 1;
        if (isRevealed(board)) targetNode = getMrXLocation(board);
        else targetNode = possibleMrXLocationsList.get(rand.nextInt(possibleMrXLocationsList.size()));
        Player detective = BoardHelper.getDetectiveOnPiece(board, inPlay);
        return d.shortestPathFromSourceToDestination(targetNode, detective, board)
                .getFirst(); //for refactoring in reference to passing board in

    }

    @Override
    public double score(Piece inPlay, List<Move> moves, int id, Board.GameState board) {
        if (inPlay.isMrX()) throw new IllegalArgumentException("Mr X shouldn't be minimising!");
        if (moves.size() > 0) {
            if (!(moves.stream().allMatch(x -> x.commencedBy().equals(inPlay)))) {
                throw new IllegalArgumentException("All moves should be commenced by " + inPlay);
            }
        }
        whenRevealed(board);
        if (getMrXBoundary().isEmpty()) throw new IllegalArgumentException("Boundary should not be empty!");
        int distance = getDistanceToMrX(inPlay, board); /*some distance function*/

        int countMoves = moves.size();
        return (weights.get(0) * distance) - (weights.get(1) * countMoves);
    }


}
