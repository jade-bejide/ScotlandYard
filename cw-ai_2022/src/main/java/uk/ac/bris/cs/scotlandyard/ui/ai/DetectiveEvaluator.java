package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

import java.util.List;

public class DetectiveEvaluator implements Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Integer> weights;

    DetectiveEvaluator(List<Integer> weights){
        this.weights = weights;
    }

    @Override
    public int score(Board.GameState board) {
        return 0;
    }

}
