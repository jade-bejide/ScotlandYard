package ai;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.MrXEvaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.getDetectives;

public class MrXEvaluatorTest extends AITestBase {
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

        List<Integer> shortestPath = new ArrayList<>();

        for (Player detective : getDetectives(game)) {
            shortestPath.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        //shortest paths are 1,5,6,7,7
        //mean is 5, sd = 2 (floored of sqrt(6))

        //2sds so reduced shortest path is [1,5] mean is 3

        //init mr X evaluator
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        mrX.use(TAXI);
        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy()== Piece.MrX.MRX).toList();
        //weight of 1 which is flattened to 0.5
        assert(mrXE.score(BLUE, mrXMoves, -1, game) == (3*0.5)+(mrXE.getSafeMoves(mrXMoves, game, mrX)*0.5)); //-1 should tell evaluator not to effect your test
    }

    @Test public void testCumulativeDistanceOnePlayer() {
        Player mrX = new Player(Piece.MrX.MRX, ImmutableMap.of(TAXI, 1,
                ScotlandYard.Ticket.BUS, 0,
                ScotlandYard.Ticket.UNDERGROUND, 0,
                ScotlandYard.Ticket.SECRET, 0,
                ScotlandYard.Ticket.DOUBLE, 0), 198);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        Dijkstra dijk = new Dijkstra();
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        mrX.use(TAXI);
        double oneDetectiveShortestPath = (double)dijk.shortestPathFromSourceToDestination(mrX.location(), green, game).getFirst();
        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(Piece.MrX.MRX)).toList();


        int mrXMovesSize = mrXMoves.size();
        assert (mrXE.score(GREEN, mrXMoves, -1, game) == oneDetectiveShortestPath);
    }

    @Test public void testCumulativeDistanceNoOutliers() {
        Player mrX = new Player(Piece.MrX.MRX, ImmutableMap.of(TAXI, 1,
                ScotlandYard.Ticket.BUS, 0,
                ScotlandYard.Ticket.UNDERGROUND, 0,
                ScotlandYard.Ticket.SECRET, 0,
                ScotlandYard.Ticket.DOUBLE, 0), 198);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 1);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 3);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 4);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, red, yellow, blue);

        //getting the shortest paths between each detective and Mr X
        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = getDetectives(game);

        List<Integer> shortestPaths = new ArrayList<>();

        for (Player detective : detectives) {
            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
        }

        int distanceMeans = Math.floorDiv(shortestPaths.stream().mapToInt(x -> x).sum(), shortestPaths.size());
        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(Piece.MrX.MRX)).toList();

        //init mr X evaluator
        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
        mrX.use(TAXI);
        int mrXMovesSize = mrXE.getSafeMoves(mrXMoves, game, mrX);
        var score =  mrXE.score(GREEN, mrXMoves, -1, game);
        score -= (mrXMovesSize*0.5);
        assert (score == (distanceMeans*0.5));
    }

//    @Test public void testMovesCountSameAsMrXAvailableMoves() {
//        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
//        Player blue = new Player(BLUE, defaultDetectiveTickets(), 42);
//        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 98);
//
//        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, green);
//
//        List<Move> mrXMoves = game.getAvailableMoves().stream().filter(x -> x.commencedBy() == MrX.MRX).toList();
//        int mrXMovesSize = mrXMoves.size();
//
//        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(1.0, 1.0));
//
//        double score = mrXE.score(MrX.MRX, mrXMoves, -1, game);
//
//        //Need to determine distances between mr X and the detectives to know what to divide by
//        Dijkstra dijk = new Dijkstra();
//        List<Player> detectives = getDetectives(game);
//        List<Integer> shortestPaths = new ArrayList<>();
//
//        for (Player detective : detectives) {
//            shortestPaths.add(dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game).getFirst());
//        }
//
//        int distanceMeans = Math.floorDiv(shortestPaths.stream().mapToInt(x -> x).sum(), shortestPaths.size());
//        //mean of [5,6] (the shortest paths) is 5 (floored)
//        System.out.println(score);
//        score -= (0.5*distanceMeans);
//        System.out.println(score);
//        System.out.println(mrXMovesSize*0.5);
//        assert(score == (mrXMovesSize * 0.5));
//    }

    //Weights tests, for the evaluator abstract class but using MrXEvaluator as the concrete
    //implementation, tests would be the same for Detective Evaluator

    @Test
    public void testTicketHeuristicDefaultMrXTickets() {
        //4 taxis, 3 buses, 3 undergrounds, 2, doubles, 5 secrets
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 76);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 54);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(),
                mrX, green);

        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(0.0, 0.0, 1.0));

        double score = mrXE.score(mrX.piece(), List.copyOf(game.getAvailableMoves()), -1, game);

        //using a calculator
        //total tickets (excl. taxi) - 3 + 3 + 2 + 5 = 13
        //taxis - buses 3/13; undergrounds 3/13; doubles 2/13; secrets 5/13
        //buses - 30/13; undergrounds - 30/13; doubles - 22/13; secrets - 40/13
        //ticket score should be 122/13
        System.out.println(score + " " + 122d/13d);
        double calcScore = 122/13;
        assert(score - calcScore == 0.0);
    }

    @Test
    public void testThat1TicketsReturnsTicketCountForThatTicket() {
        Player mrX = new Player(Piece.MrX.MRX, ImmutableMap.of(
                TAXI, 0,
                ScotlandYard.Ticket.BUS, 1,
                ScotlandYard.Ticket.UNDERGROUND, 0,
                ScotlandYard.Ticket.SECRET, 0,
                ScotlandYard.Ticket.DOUBLE, 0), 76);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 54);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(),
                mrX, green);

        MrXEvaluator mrXE = new MrXEvaluator(Arrays.asList(0.0, 0.0, 1.0));

        double score = mrXE.score(Piece.MrX.MRX, List.copyOf(game.getAvailableMoves()), -1, game);
        assert(score == mrX.tickets().get(BUS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNegativeWeights() {
        //erroneous
        //no negative weights for now
        MrXEvaluator w = new MrXEvaluator(Arrays.asList(-1.0,2.0, 1.0));

    }

    //For the following two tests
    //There should be at least one non-zero weight possible
    @Test(expected = IllegalArgumentException.class)
    public void testAllZeroWeightsNotPossible() {
        MrXEvaluator w = new MrXEvaluator(Arrays.asList(0.0,0.0, 0.0));
    }

    @Test
    public void testSomeZeroWeightsPossible() {
        MrXEvaluator w1 = new MrXEvaluator(Arrays.asList(0.0, 1.0, 1.0));
        MrXEvaluator w2 = new MrXEvaluator(Arrays.asList(1.0, 0.0, 1.0));
        MrXEvaluator w3 = new MrXEvaluator(Arrays.asList(1.0, 1.0, 0.0));
        MrXEvaluator w4 = new MrXEvaluator(Arrays.asList(0.0, 0.0, 1.0));
        MrXEvaluator w5 = new MrXEvaluator(Arrays.asList(1.0, 0.0, 0.0));
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
