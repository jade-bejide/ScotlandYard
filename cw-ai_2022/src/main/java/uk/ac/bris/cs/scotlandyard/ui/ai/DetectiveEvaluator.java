package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.*;

public class DetectiveEvaluator implements Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Integer> weights;
    private Set<Integer> possibleMrXLocations = new HashSet<Integer>();

    DetectiveEvaluator(List<Integer> weights){
        this.weights = weights;
    }

    //get and set boundaries
    public void setMrXBoundary(Move.SingleMove revealedMove, Board.GameState board) {
        int revealedLocation = revealedMove.destination;

        Set<Integer> boundary = new HashSet<Integer>(board.getSetup().graph.successors(revealedLocation));

        for (Integer node : boundary) {
            boundary.addAll(board.getSetup().graph.successors(node));
        }

        possibleMrXLocations = boundary;
    }

    public void setMrXBoundary(Move.DoubleMove revealedMove, Board.GameState board) {
        Integer revealedLocation = revealedMove.destination2;

        Set<Integer> boundary = new HashSet<Integer>(board.getSetup().graph.successors(revealedLocation));

        for (Integer node : boundary) {
            boundary.addAll(board.getSetup().graph.successors(node));
        }

        possibleMrXLocations = boundary;
    }

    public Set<Integer> getMrXBoundary() {
        return possibleMrXLocations;
    }

    private int getDistanceToMrX(Piece inPlay, Board.GameState board){
        Random rand = new Random();
        List<Integer> possibleMrXLocationsList = new ArrayList<Integer>(possibleMrXLocations);
        Integer targetNode = possibleMrXLocationsList.get(rand.nextInt(possibleMrXLocations.size()));
        Player detective = BoardHelper.getDetectiveOnPiece(board, inPlay);
        return d.shortestPathFromSourceToDestination(board.getSetup().graph, detective.location(), targetNode, detective, board)
                .getFirst(); //for refactoring in reference to passing board in

    }

    @Override
    public int score(Piece inPlay, Board.GameState board) {
        int distance = getDistanceToMrX(inPlay, board); /*some distance function*/;
        int moveCount = board.getAvailableMoves()
                .stream()
                .filter(x -> x.commencedBy().equals(inPlay))
                .toList()
                .size();

        return 0;
    }

}
