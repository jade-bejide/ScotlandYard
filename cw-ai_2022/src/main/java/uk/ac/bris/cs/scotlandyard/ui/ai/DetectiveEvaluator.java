package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.print.attribute.IntegerSyntax;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DetectiveEvaluator extends Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Double> weights;
    private Set<Integer> possibleMrXLocations = new HashSet<Integer>();

    public DetectiveEvaluator(List<Double> weights){
        this.weights = flatten(weights);
        for(int i = 1; i < 200; i++) {
            this.possibleMrXLocations.add(i);
        }
    }

    //get and set boundaries

    public void setMrXBoundary(Integer revealedLocation, Board.GameState board) {
        Set<Integer> boundary = new HashSet<Integer>(board.getSetup().graph.successors(revealedLocation));

        Set<Integer> boundaryCpy = Set.copyOf(boundary);
        for (Integer node : boundaryCpy) {
            boundary.addAll(board.getSetup().graph.successors(node));
        }

        possibleMrXLocations = boundary;
    }

//    public Set<Integer> getMrXBoundary() {
//        return possibleMrXLocations;
//    }

    //checks if Mr X's location has been revealed and update Mr X boundary as appropriate
    private void isRevealed(Board.GameState board) {
        ImmutableList<LogEntry> log = board.getMrXTravelLog();
        int n = log.size();

        LogEntry currentLog = log.get(n-1);
        if (currentLog.location().isPresent()) {
            if (!log.get(n-1).location().isEmpty()) setMrXBoundary(currentLog.location().get(), board);
        }
    }

    private int getDistanceToMrX(Piece inPlay, Board.GameState board){
        Random rand = new Random();
        List<Integer> possibleMrXLocationsList = new ArrayList<Integer>(possibleMrXLocations);
        Integer targetNode = possibleMrXLocationsList.get(rand.nextInt(possibleMrXLocationsList.size()));
        Player detective = BoardHelper.getDetectiveOnPiece(board, inPlay);
        return d.shortestPathFromSourceToDestination(targetNode, detective, board)
                .getFirst(); //for refactoring in reference to passing board in

    }

    @Override
    public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
        isRevealed(board);
        int distance = getDistanceToMrX(inPlay, board); /*some distance function*/;
        int countMoves = moves.size();

        return (weights.get(0) * distance) - (weights.get(1) * countMoves);
    }

}
