package ai;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
public class MiniMaxBoxTest {

    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;

    private Board getSetup(){
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (
                IOException e) { throw new RuntimeException("Unable to read game graph", e); }

        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 138);
        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        Board.GameState state = new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, red, green, blue, white, yellow);
        return state;
    }

    private Board getSmallSetup(){
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (
                IOException e) { throw new RuntimeException("Unable to read game graph", e); }

        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var mrX = new Player(MRX, defaultMrXTickets(), 92);
        var green = new Player(GREEN, defaultDetectiveTickets(), 93);
        Board.GameState state = new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, red, green);
        return state;
    }

    @Test
    public void testTurnsGenerateCorrectly(){
        Board board = getSetup();
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5))
        );
        List<Turn> turns = minimax.getTurns(7, (Board.GameState) board); //does just over one loop
        assertTrue(turns.get(0).playedBy().equals(MRX) &&
                turns.get(turns.size() - 1).playedBy().equals(MRX) &&
                turns.subList(1, turns.size() - 1)
                        .stream()
                        .map(Turn::playedBy)
                        .toList()
                        .containsAll(Arrays.asList(RED, GREEN, BLUE, WHITE, YELLOW)));
    }

//    @Test public void testEvaluatorsAssignCorrectly(){
//        Board board = getSetup();
//        MiniMaxBox minimax = MiniMaxBox.getInstance(
//                new MrXEvaluator(Arrays.asList(0.5, 0.5)),
//                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
//                new DoubleTree(), new DoubleTree()
//        );
//        List<Turn> turns = minimax.getTurns(6, (Board.GameState) board);
//        assertTrue(turns.get(0).evaluator().equals(minimax.getMrXEvaluator()) &&
//                turns.subList(1, turns.size())
//                        .stream()
//                        .map(Turn::evaluator)
//                        .allMatch(x -> x.equals(minimax.getDetectiveEvaluator())));
//    }

//    @Test public void testMiniMaxChoice(){
//        Board board = getSetup();
//        minimax.minimax(4, (Board.GameState) board);
//    }
//

    @Test (expected = AssertionError.class)
    public void testMiniMaxTooManyTreesShouldThrow(){
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree(), new DoubleTree()
        );
    }

    @Test
    public void testMiniMaxRecursionToTree(){
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree()
        );
        Board.GameState board = (Board.GameState) getSmallSetup();
        List<Move> moves = minimax.minimax(2, board);
        board = board.advance(moves.get(0));
        minimax.minimax(2, board);
        DoubleTree tree = minimax.getTree();
        tree.show();
        assertFalse(tree.equals(new DoubleTree())); //check that it has changed
    }


}
