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
		Evaluator mrXBrain = new MrXEvaluator(Arrays.asList(0.5, 0.5));
		Evaluator detectiveBrain = new DetectiveEvaluator(Arrays.asList(0.5, 0.5));
		//Evaluator findMrXforDetectives =
		MiniMaxBox miniMaxBox = MiniMaxBox.getInstance(mrXBrain, detectiveBrain);

		return miniMaxBox.minimax(5, (Board.GameState) board).get(0); //first move in optimal moves

	}
}