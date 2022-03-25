package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

public interface Evaluator { //for static evaluation anoymous classes and strategy pattern
    public int getScore(Board.GameState board);
}
