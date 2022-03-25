package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.List;

public class MiniMaxBox {
    /*minimax handler is a stateful singleton class
    it defines a recursion depth for the algorithm,
    it should remember when we've asked it to predict detective's moves,
    the minimax algorithm can predict lists of optimal moves at once which optimises the process of moving multiple
    detectives in the same turn. it will store these moves until it needs to calculate more (recursion depth correlates
    to number of moves calculated.
     */
    static private MiniMaxBox instance = null;

    private final int depth = 3; //recursion depth
    private List<Move> preComputedMoves;
    private final Evaluator evaluator;

    private MiniMaxBox(Evaluator evaluator){

    }

    static MiniMaxBox getInstance(Evaluator... evaluators){ //singleton
        Evaluator evaluator = evaluators[0]; //if someone mistakenly passes lots of evaluators we only want the first
        if(evaluators.length > 1) System.out.println("Warning: MiniMaxBox will take the first of " + evaluators.length + " evaluators.");
        if(instance == null) { instance = new MiniMaxBox(evaluator); }
        return instance;
    }

    private
}
