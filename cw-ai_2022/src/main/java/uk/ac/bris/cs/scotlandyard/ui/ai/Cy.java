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
		long start = System.currentTimeMillis();
		long end = start + (timeoutPair.left()-1) * 1000;
//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		Evaluator mrXBrain = new MrXEvaluator(Arrays.asList(4.0, 4.0, 2.0));
		Evaluator detectiveBrain = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
		//Evaluator findMrXforDetectives =
		MiniMaxBox miniMaxBox = MiniMaxBox.renewInstance(mrXBrain, detectiveBrain);
	    //first move in optimal moves as goes continue (move taken this turn by this player)
		Move chosenMove = null;

		while (System.currentTimeMillis() < end && chosenMove == null) {
			chosenMove = miniMaxBox.minimax(5, (Board.GameState) board).get(0);
		}
		if (chosenMove != null) return chosenMove;
		else {
			System.out.println("Oh no");
			var moves = board.getAvailableMoves().asList();
			return moves.get(new Random().nextInt(moves.size()));
		}

	}
}
