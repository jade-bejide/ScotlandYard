package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class Cy implements Ai {


	@Nonnull @Override public String name() { return "Cy"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		long start = System.currentTimeMillis();
		long end = start + ((timeoutPair.left()/2) * 1000);
		Evaluator mrXBrain = new MrXEvaluator(Arrays.asList(4.0, 4.0, 2.0));
		Evaluator detectiveBrain = new DetectiveEvaluator(Arrays.asList(3.0, 1.0));
		MiniMaxBox miniMaxBox = new MiniMaxBox(mrXBrain, detectiveBrain);
	    //first move in optimal moves as goes continue (move taken this turn by this player)

		Move chosenMove;
		//How to exit on time?
		while (System.currentTimeMillis() < end) {
				chosenMove = miniMaxBox.minimax(4, (Board.GameState) board).get(0);
				return chosenMove;

		}

		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));


	}
}
