package ai;

import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.MrXEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.getDetectiveOnPiece;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.getDetectives;

public class MrXEvaluatorTest extends RenameMe {
    @Test
    public void testCumulativeDistanceOutliers() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 32);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 54);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 34);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 21);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, red, yellow, white, blue);

        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();

        //shortest paths are 1,5,6,7,7
        //mean is 5, sd = 1 (floored of sqrt(2))

        //init mr X evaluator
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        //weight of 1 which is flattened to 0.5
        assert(mrXE.score(BLUE, mrXMoves, game) == (1*0.5) + (mrXMovesSize* 0.5));
    }

    @Test public void testCumulativeDistanceOnePlayer() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        Dijkstra dijk = new Dijkstra();
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        Integer oneDetectiveShortestPath = dijk.shortestPathFromSourceToDestination(mrX.location(), green, game).getFirst();
        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();
        assert (mrXE.score(GREEN, mrXMoves, game) == (oneDetectiveShortestPath*0.5) + (mrXMovesSize*0.5));
    }

    @Test public void testCumulativeDistanceNoOutliers() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 1);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 3);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 4);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, red, yellow, blue);

        //gettng shortest paths between each detective and Mr X
        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = getDetectives(game);

        List<Integer> shortestPaths = new ArrayList<>();

        for (Player detective : detectives) {
            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        int distanceMeans = Math.floorDiv(shortestPaths.stream().mapToInt(x -> x).sum(), shortestPaths.size());
        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();

        //init mr X evaluator
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        //weight of 1 which is flattened to 0.5
        //sd - 1 new list [5 6 6] -> final mean is 5, 2.5 using the weighted value
        var score =  mrXE.score(GREEN, mrXMoves, game);
        score -= (mrXMovesSize*0.5);
        assert (score == (distanceMeans*0.5));
    }

    @Test public void testMovesCountSameAsMrXAvailableMoves() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 42);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 98);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, green);

        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy() == MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();

        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));

        double score = mrXE.score(MrX.MRX, mrXMoves, game);

        //Need to determine distances between mr X and the detectives to know what to divide by
        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = getDetectives(game);
        List<Integer> shortestPaths = new ArrayList<>();

        for (Player detective : detectives) {
            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        int distanceMeans = Math.floorDiv(shortestPaths.stream().mapToInt(x -> x).sum(), shortestPaths.size());
        //mean of [5,6] (the shortest paths) is 5 (floored)
        score -= (0.5*distanceMeans);
        assert(score == (mrXMovesSize * 0.5));
    }

    //Weights tests, for the evaluator abstract class but using MrXEvaluator as the concrete
    //implementation, tests would be the same for Detective Evaluator

    @Test(expected = IllegalArgumentException.class)
    public void testNoNegativeWeights() {
        //erroneous
        //no negative weights for now
        MrXEvaluator w5 = new MrXEvaluator(Arrays.asList(-1.0,2.0));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoZeroWeights() {
        MrXEvaluator w6 = new MrXEvaluator(Arrays.asList(0.0,0.0));
    }

    @Test
    public void testWeightsFlattenCorrectly() {
        //normal
        MrXEvaluator w1 = new MrXEvaluator(Arrays.asList(1.0,2.0));

        List<Double> w1Flattened = w1.getWeights();
        double w11C = 1d / 3;
        double w12C = 2d / 3;

        assert(w1Flattened.get(0) == w11C);
        assert(w1Flattened.get(1) == w12C);

        MrXEvaluator w4 = new MrXEvaluator(Arrays.asList(1.0,3.0));

        List<Double> w4Flattened = w4.getWeights();
        double w41C = 1d/4;
        double w42C = 3d/4;

        assert(w4Flattened.get(0) == w41C);
        assert(w4Flattened.get(1) == w42C);

        //boundary
        MrXEvaluator w2 = new MrXEvaluator(Arrays.asList(1.0,1.0));
        MrXEvaluator w3 = new MrXEvaluator(Arrays.asList(2.0,2.0));

        List<Double> w2Flattened = w2.getWeights();
        List<Double> w3Flattened = w3.getWeights();

        assert (w2Flattened.get(0).equals(w3Flattened.get(0)) &&
                w2Flattened.get(0) == 0.5 &&
                w2Flattened.get(1).equals(w3Flattened.get(1)));

    }

}
