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
		Evaluator mrXBrain = new MrXEvaluator(Arrays.asList(4.0, 4.0, 2.0));
		Evaluator detectiveBrain = new DetectiveEvaluator(Arrays.asList(3.0, 1.0));
		MiniMaxBox miniMaxBox = new MiniMaxBox(mrXBrain, detectiveBrain);

		return miniMaxBox.minimax(4, (Board.GameState) board).get(0);
	}
}
