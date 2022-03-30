package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {
	@Nonnull @Override public String name() { return "Cy"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation

//		var moves = board.getAvailableMoves().asList();
//		return moves.get(new Random().nextInt(moves.size()));
		Evaluator mrXBrain = new AdapterMrX(Arrays.asList(1, 2));
		Evaluator detectiveBrain = new AdapterDetective(Arrays.asList(1, 2));
		//Evaluator findMrXforDetectives =
		MiniMaxBox miniMaxBox = MiniMaxBox.getInstance(mrXBrain, mrXBrain);

		return miniMaxBox.minimax(3, (Board.GameState) board);

	}
}
