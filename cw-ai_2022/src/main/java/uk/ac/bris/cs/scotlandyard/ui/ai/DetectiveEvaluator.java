package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    public int score(Board.GameState board) {
        return 0;
    }

}
