package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.List;

public interface Evaluator { //for static evaluation anoymous classes and strategy pattern
    public double score(Piece inPlay, List<Move> moves, Board.GameState board);
}
