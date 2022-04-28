package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.List;
import java.util.stream.Collectors;

//Here we are making use of an abstract class because the behaviour for flatten
//is the same across all Evaluators whereas the score method differs across concrete
//implementations of Evaluator
public abstract class Evaluator { //for static evaluation anoymous classes and strategy pattern
    public List<Double> flatten(List<Double> weights) { //set weights to sum to 1
        if (weights.stream().allMatch(x -> x == 0.0)) throw new IllegalArgumentException("Weights cannot all be zero!");
        if (weights.stream().anyMatch(x -> x < 0)) throw new IllegalArgumentException("No negative weights allowed!");

        double total = 0;
        for(Double d : weights) total += d;
        final double finalTotal = total;
        return weights.stream().map(x -> x / finalTotal).collect(Collectors.toList());
    }
    public abstract double score(Piece inPlay, List<Move> moves, int id, Board.GameState board);
}
