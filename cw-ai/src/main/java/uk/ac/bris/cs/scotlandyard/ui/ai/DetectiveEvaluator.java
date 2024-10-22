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
        Set<Integer> boundaryCpy = boundary;
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

        //if boundary is empty just fall back to the original boundary
        if (boundary.size() > 0) return boundary;
        else return boundaryCpy;
    }

    //refocuses mr X's boundary each time he reveals himself
    public void setMrXBoundary(Integer revealedLocation, Board.GameState board, boolean isFiltering) {
        if (revealedLocation < 1 || revealedLocation > 199) throw new IllegalArgumentException("Not a valid location");

        //boundary is now one set of successors wide, to focus the move pool
        Set<Integer> boundary = new HashSet<Integer>(board.getSetup().graph.successors(revealedLocation));

        if (isFiltering) boundary = filterBoundary(board, boundary, revealedLocation);
        if (isFiltering) boundary = filterBoundary(board, boundary, revealedLocation);
        possibleMrXLocations = boundary;
    }

    //needed for testing
    public Set<Integer> getMrXBoundary() {
        return possibleMrXLocations;
    }

    //checks if Mr X's location has been revealed and update Mr X boundary as appropriate
    private void whenRevealed(Board.GameState board) {
        if (BoardHelper.getLastRevealedLog(board).location().isPresent()) setMrXBoundary(BoardHelper.getLastRevealedLog(board).location().get(), board, false);
    }

    public int getMrXLocation(Board.GameState board) {
        LogEntry currentLastReveal = BoardHelper.getLastRevealedLog(board);
        return currentLastReveal.location().isPresent() ? currentLastReveal.location().get() : 1;

    }

    public boolean isRevealed(Board.GameState board) {
        int n = board.getMrXTravelLog().size();
        if (n > 0) return BoardHelper.getLastRevealedLog(board).location().isPresent();
        else return false;
    }

    //for static evaluation, calculate distance to mr X
    private int getDistanceToMrX(Piece inPlay, Board.GameState board){
        Random rand = new Random();
        List<Integer> possibleMrXLocationsList = new ArrayList<Integer>(possibleMrXLocations);
        //here they all decide that mr X is potentially at different locations rather than having a common goal
        Integer targetNode;
        if (isRevealed(board)) targetNode = getMrXLocation(board);
        else targetNode = possibleMrXLocationsList.get(rand.nextInt(possibleMrXLocationsList.size()));
        Player detective = BoardHelper.getDetectiveOnPiece(board, inPlay);
        return d.shortestPathFromSourceToDestination(targetNode, detective, board)
                .getFirst();

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
        int distance = getDistanceToMrX(inPlay, board); //distance heuristic

        int countMoves = moves.size();
        return (weights.get(0) * distance) - (weights.get(1) * countMoves);
    }


}
