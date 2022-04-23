package ai;

import com.google.common.collect.ImmutableMap;
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
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var mrX = new Player(MRX, defaultMrXTickets(), 2);
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

    @Test
    public void testEvaluatorsAssignCorrectly(){
        Board.GameState board = (Board.GameState) getSetup(); //mrX turn first
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5, 1.0)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree()
        );
        for(int i = 0; i < 2; i++) { //test twice to check if it can assign both types of evaluator right
            Piece who = minimax.getTurns(1, board).get(0).playedBy(); //whos go is it this round
            board = board.advance(minimax.minimax(4, board).get(0));
            Evaluator evaluator = minimax.getThisTurnStrategy();
            try {
                //instead of an intrusive test visitor,
                // we can use this try catch using casting to decide underlying type
                if (who.isMrX()) {
                    MrXEvaluator x = (MrXEvaluator) evaluator;
                } else {
                    DetectiveEvaluator x = (DetectiveEvaluator) evaluator;
                }
            } catch (ClassCastException e) {
                throw new AssertionError("Assigned the wrong evaluator on this turn \n" + e);
            }
        }
    }

    @Test (expected = AssertionError.class)
    public void testMiniMaxTooManyTreesShouldThrow(){
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5, 1.0)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree(), new DoubleTree()
        );
    }

    @Test
    public void testMiniMaxRecursionToTree(){
//        Evaluator simpleMrXEvaluator = new Evaluator() {
//            @Override
//            public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
//                return BoardHelper.getMrX(board);
//            }
//        };
        Evaluator simpleDetectiveEvaluator = new Evaluator() {
            @Override
            public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
                return BoardHelper.getDetectives(board).stream()
                        .filter(x -> x.piece().equals(inPlay))
                        .toList().get(0).location();
            }
        };
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5, 0.5)),
                simpleDetectiveEvaluator,
                new DoubleTree()
        );
        Board.GameState board = (Board.GameState) getSmallSetup();
        List<Move> moves = minimax.minimax(3, board);
        board = board.advance(moves.get(0));
        //minimax.minimax(1, board);
        DoubleTree tree = minimax.getTree();
        tree.show();
        assertFalse(tree.equals(new DoubleTree())); //check that it has changed
    }

    //testing specific setups
    @Test
    public void testEndOfGameDecisionTree(){
        // anonymous evaluators here allows complete control over the generated evaluations
        Evaluator simpleMrXEvaluator = new Evaluator() {
            @Override
            public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
                return BoardHelper.getMrX(board, moves.get(0).source()).location();
            }
        };
        MiniMaxBox minimax = MiniMaxBox.getInstance(
                new MrXEvaluator(Arrays.asList(6.0, 3.0, 1.0)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree()
        );
        var mrX = new Player(MRX, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 0,
                Ticket.BUS, 0,
                Ticket.UNDERGROUND, 1,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0),
                1);
        var blue = new Player(BLUE, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 1,
                Ticket.BUS, 0,
                Ticket.UNDERGROUND, 0,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0)
                , 8);
        Board.GameState board = new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, blue); //two detectives limited moves

        board = board.advance(minimax.minimax(1, board).get(0));
        mrX.give(Ticket.TAXI);
        board.advance(minimax.minimax(2, board).get(0));
        minimax.getTree().show();
//        DoubleTree expectedMiniMaxTree = new DoubleTree(new Node(1,
//                Arrays.asList(new Node())));
    }

}
