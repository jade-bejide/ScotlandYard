package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;

import java.util.List;

public class AdapterDetective implements Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting
    private final List<Integer> weights;

    AdapterDetective(List<Integer> weights){
        this.weights = weights;
    }

    @Override
    public int score(Board.GameState board) {
        return 0;
    }

}
