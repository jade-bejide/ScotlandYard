package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class Cy implements Ai {
	@Nonnull @Override public String name() { return "Cy"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation

//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		Evaluator mrXBrain = new MrXEvaluator(Arrays.asList(4.0, 4.0, 2.0));
		Evaluator detectiveBrain = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
		//Evaluator findMrXforDetectives =
		MiniMaxBox miniMaxBox = MiniMaxBox.getInstance(mrXBrain, detectiveBrain);
	    //first move in optimal moves as goes continue (move taken this turn by this player)
		return miniMaxBox.minimax(4, (Board.GameState) board).get(0);

	}
}
