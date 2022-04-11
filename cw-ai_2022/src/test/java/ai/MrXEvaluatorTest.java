package ai;

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

        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = game.getPlayers().stream().filter(x -> !x.isMrX()).map(x -> getDetectiveOnPiece(game, x)).toList();

        List<Integer> shortestPaths = new ArrayList<>();
        for (Player detective : detectives) {
            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();


        //shortest paths are 1,5,6,7,7
        //mean is 5, sd = 2 (floored)

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
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 2);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 3);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 4);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 5);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, red, yellow, white, blue);

        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = game.getPlayers().stream().filter(x -> !x.isMrX()).map(x -> getDetectiveOnPiece(game, x)).toList();

        List<Integer> shortestPaths = new ArrayList<>();
        for (Player detective : detectives) {
            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        int mrXMovesSize = mrXMoves.size();

        System.out.println(mrXMovesSize);

        //init mr X evaluator
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        //weight of 1 which is flattened to 0.5
        System.out.println(shortestPaths);
        var score =  mrXE.score(GREEN, mrXMoves, game);
        score -= (mrXMovesSize*0.5);
        System.out.println(score);
    }
}
