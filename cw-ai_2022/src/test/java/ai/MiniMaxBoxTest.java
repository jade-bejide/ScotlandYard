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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
        return new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, red, green, blue, white, yellow);
    }

    private Board getSmallSetup(){
        var mrX = new Player(MRX, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 0,
                Ticket.BUS, 2,
                Ticket.UNDERGROUND, 0,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0),
                1);
        var blue = new Player(BLUE, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 2,
                Ticket.BUS, 0,
                Ticket.UNDERGROUND, 0,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0)
                , 44); //will block some of mrx's move safety
        return new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, blue);
    }

    private MiniMaxBox foldUpMiniMaxBox(Evaluator mrxBrain, Evaluator detectiveBrain){
        return MiniMaxBox.getInstance(
                mrxBrain,
                detectiveBrain,
                new DoubleTree()
        );
    }

    private Node getExtremalBranch(DoubleTree tree, Comparator<Node> cmprt){
        return tree.getNodeOnLocation().getBranches().stream()
                .max(cmprt)
                .stream()
                .toList()
                .get(0);
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
        MiniMaxBox minimax = MiniMaxBox.renewInstance(
                new MrXEvaluator(Arrays.asList(0.5, 0.5, 1.0)),
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree()
        );
        for(int i = 0; i < 2; i++) { //test twice to check if it can assign both types of evaluator right
            Piece who = minimax.getTurns(1, board).get(0).playedBy(); //whos go is it this round
            board = board.advance(minimax.minimax(4, board).get(0));
            Evaluator evaluator = minimax.getThisTurnStrategy();
            try {
                if (who.isMrX()) {
                    MrXEvaluator x = (MrXEvaluator) evaluator;
                } else {
                    DetectiveEvaluator y = (DetectiveEvaluator) evaluator;
                }
            } catch (ClassCastException e) {
                throw new AssertionError("Assigned the wrong evaluator on this turn \n" + e);
            }
        }
    }

    @Test (expected = AssertionError.class)
    public void testMiniMaxTooManyTreesShouldThrow(){
        MiniMaxBox minimax = MiniMaxBox.renewInstance(
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
            public double score(Piece inPlay, List<Move> moves, int id, Board.GameState board) {
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

    @Test
    public void testMrXMaximises(){
        Board.GameState board = (Board.GameState) getSetup();
        MiniMaxBox miniMaxBox = foldUpMiniMaxBox(new MrXEvaluator(Arrays.asList(1.0, 1.0, 1.0)), new DetectiveEvaluator(Arrays.asList(1.0, 1.0)));
        board.advance(miniMaxBox.minimax(4, board).get(0));

        DoubleTree tree = miniMaxBox.getTree();
        for(Node node : tree.getNodeOnLocation().getBranches()){ node.pruneAllChildren(); }
        Node largestBranch = getExtremalBranch(tree, Comparator.comparingDouble(Node::getValue));
        Node miniMaxTreeRoot = miniMaxBox.getTree().getNodeOnLocation();
        miniMaxTreeRoot.pruneAllChildren();
        DoubleTree root = new DoubleTree(miniMaxTreeRoot);
        DoubleTree chosen = new DoubleTree(largestBranch);

        assert(root.equals(chosen));
    }

    @Test
    public void testDetectiveMinimises(){
        Board.GameState board = (Board.GameState) getSmallSetup();
        MiniMaxBox miniMaxBox = foldUpMiniMaxBox(new MrXEvaluator(Arrays.asList(1.0, 1.0, 1.0)), new DetectiveEvaluator(Arrays.asList(1.0, 1.0)));
        board = board.advance(miniMaxBox.minimax(4, board).get(0));
        miniMaxBox.minimax(4, board);

        DoubleTree tree = miniMaxBox.getTree();
        for(Node node : tree.getNodeOnLocation().getBranches()){ node.pruneAllChildren(); }
        Node smallestBranch = getExtremalBranch(tree, Comparator.comparingDouble(x -> -x.getValue()));
        Node miniMaxTreeRoot = miniMaxBox.getTree().getNodeOnLocation();
        miniMaxTreeRoot.pruneAllChildren();
        DoubleTree root = new DoubleTree(miniMaxTreeRoot);
        DoubleTree chosen = new DoubleTree(smallestBranch);

        assert(root.equals(chosen));
    }

    @Test
    public void testPruning(){
//        Evaluator simpleMrXEvaluator = new Evaluator() {
//            static class DestinationChecker implements Move.Visitor<Integer> {
//                @Override
//                public Integer visit(Move.SingleMove move) {
//                    return move.destination;
//                }
//
//                //Note that this will never be called
//                @Override
//                public Integer visit(Move.DoubleMove move) {
//                    return move.destination2;
//                }
//            }
//            @Override
//            public double score(Piece inPlay, List<Move> moves, int id, Board.GameState board) {
//                return moves.get(id).accept(new DestinationChecker());
//            }
//        };
        var mrX = new Player(MRX, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 1,
                Ticket.BUS, 1,
                Ticket.UNDERGROUND, 0,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0),
                2);
        var blue = new Player(BLUE, ImmutableMap.of(ScotlandYard.Ticket.TAXI, 1,
                Ticket.BUS, 0,
                Ticket.UNDERGROUND, 0,
                Ticket.SECRET, 0,
                Ticket.DOUBLE, 0)
                , 21); //will block some of mrx's move safety
        Board.GameState board = new MyGameStateFactory().build(RenameMe.standard24MoveSetup(),
                mrX, blue);
        MiniMaxBox miniMaxBox = foldUpMiniMaxBox(new MrXEvaluator(Arrays.asList(1.0, 0.0, 0.0)), new DetectiveEvaluator(Arrays.asList(1.0, 0.0)));
        miniMaxBox.minimax(2, board);
        miniMaxBox.getTree().show();
    }

    //testing specific setups
    @Test
    public void testEndOfGameDecisionTree(){
        Board.GameState board = (Board.GameState) getSmallSetup();
        // anonymous evaluators here allows complete control over the generated evaluations
        Evaluator simpleMrXEvaluator = new Evaluator() {
            @Override
            public double score(Piece inPlay, List<Move> moves, int id, Board.GameState board) {
                return id;
            }
        };
        MiniMaxBox minimax = MiniMaxBox.getInstance(
//                new MrXEvaluator(Arrays.asList(0.0, 1.0, 0.0)), //test on movement freedom
                simpleMrXEvaluator,
                new DetectiveEvaluator(Arrays.asList(0.5, 0.5)),
                new DoubleTree()
        );
        board = board.advance(minimax.minimax(2, board).get(0));
        minimax.getTree().show();
    }

}
