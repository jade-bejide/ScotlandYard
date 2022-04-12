package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Evaluator { //for static evaluation anoymous classes and strategy pattern
    public List<Double> flatten(List<Double> weights) { //set weights to sum to 1
        if (weights.contains(0.0)) throw new IllegalArgumentException("No zero weights allowed!");
        if (weights.stream().anyMatch(x -> x < 0)) throw new IllegalArgumentException("No negative weights allowed!");

        double total = 0;
        for(Double d : weights) total += d;
        final double finalTotal = total;
        return weights.stream().map(x -> x / finalTotal).collect(Collectors.toList());
    }
    public abstract double score(Piece inPlay, List<Move> moves, Board.GameState board);
}
