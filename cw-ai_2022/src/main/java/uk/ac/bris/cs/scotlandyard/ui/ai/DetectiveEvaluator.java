package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.Player;

import java.util.*;

public class DetectiveEvaluator implements Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Integer> weights;
    private Set<Integer> possibleMrXLocations = new HashSet<Integer>();

    DetectiveEvaluator(List<Integer> weights){
        this.weights = weights;
    }

    private int getDistanceToMrX(Piece inPlay, Board.GameState board){
        Random rand = new Random();
        List<Integer> possibleMrXLocationsList = new ArrayList<Integer>(possibleMrXLocations);
        Integer targetNode = possibleMrXLocationsList.get(rand.nextInt(possibleMrXLocations.size()));
        return d.shortestPathFromSourceToDestination(board.getSetup().graph, BoardHelper.getDetectiveOnPiece(inPlay), targetNode, inPlay, board)
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


    }

}
