package ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.DetectiveEvaluator;

import javax.print.attribute.standard.Destination;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;


public class DetectivesEvaluatorTest extends AITestBase {
    @Test
    public void testStartBoundaryContainsWholeGraph(){
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));

        //There are 199 nodes, the natural sum of 199 is 199(200)/2 = 19900
        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() == 19900);
    }

    @Test
    public void testBoundaryUpdates() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(176, game, true);

        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() != 19900);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoIllegalRevealedLocationAllowed() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(-2, game, true);
    }

    //will test that set boundary narrows when mr X reveals himself
    @Test
    public void testBoundaryNarrowsWhenRevealed() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(18, game, true);

        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() != 19900);
    }

    //test that the boundary encompasses ALL of mr X available locations
    @Test
    public void testBoundaryProducedCorrectly() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 18);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(54, game, false);
        assert(dE.getMrXBoundary().containsAll(Set.of(41, 70, 55, 53)));
    }

    //will test that set boundary matches expected given needed filtering
    @Test
    public void testBoundaryFilteredCorrectly() {
        Player mrX = new Player(Piece.MrX.MRX, makeTickets(0, 3, 3, 2, 3), 56);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 55);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 108);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, blue);

        DetectiveEvaluator dEFiltering = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));

        //situation 1 - complete filtering
        dEFiltering.setMrXBoundary(24, game, true);
        Set<Integer> filteredBoundary = dEFiltering.getMrXBoundary();

        //filtered boundary would be empty so fall back to unfiltered boundary
        assert(filteredBoundary.containsAll(game.getSetup().graph.adjacentNodes(24)));

        //situation 2 - partial filtering
        dEFiltering.setMrXBoundary(22, game, true);
        filteredBoundary = dEFiltering.getMrXBoundary();
        assert(!filteredBoundary.containsAll(Set.of(35, 65, 11, 3, 23, 13, 34, 47, 10, 48, 36, 37, 12)));
    }

    //Test the boundary only updates when lawful
    @Test
    public void testBoundaryOnlyUpdatesOnCorrectRounds() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 69);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 49);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));

        //shouldn't update on second turn
        game = game.advance(new Move.SingleMove(MrX.MRX, mrX.location(), TAXI, 86));
        dE.score(GREEN, List.copyOf(game.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(GREEN)).toList()), -1, game); //id -1 should flag evaluator not to effect your tests
        assert(game.getMrXTravelLog().size() == 1);
        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() == 19900);

//        //should update on round 3, for example

        game = game.advance(new Move.SingleMove(GREEN, green.location(), TAXI, 36));
        game = game.advance(new Move.SingleMove(MrX.MRX, 86, BUS, 87));

        game = game.advance(new Move.SingleMove(GREEN, 36, TAXI, 35));
        game = game.advance(new Move.SingleMove(MrX.MRX, 87, BUS, 105));
        dE.score(GREEN, List.copyOf(game.getAvailableMoves()), -1, game);
        assert(game.getMrXTravelLog().size() == 3);
        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() != 19900);

    }

    @Test
    public void testRevealIsLawful() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 69);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 49);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));

        //Before Mr X goes
        assert(!dE.isRevealed(game));

        //Round 1
        game = game.advance(game.getAvailableMoves().asList().get(0));
        game = game.advance(game.getAvailableMoves().asList().get(0));

        assert(!dE.isRevealed(game));

        //Round 2
        game = game.advance(game.getAvailableMoves().asList().get(0));
        game = game.advance(game.getAvailableMoves().asList().get(0));

        assert(!dE.isRevealed(game));

        //Round 3
        game = game.advance(game.getAvailableMoves().asList().get(0));
        game = game.advance(game.getAvailableMoves().asList().get(0));

        assert(dE.isRevealed(game));

    }

    //check that detectives can see the correct location for mr X on a reveal move
    @Test
    public void testThatMrXLocationIsRevealedCorrectly() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 69);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 49);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));

        //Round 1
        game = game.advance(game.getAvailableMoves().asList().get(0));
        game = game.advance(game.getAvailableMoves().asList().get(0));

        //Round 2
        game = game.advance(game.getAvailableMoves().asList().get(0));
        game = game.advance(game.getAvailableMoves().asList().get(0));

        //this would not be could if the log is not on a reveal move, but still checking that
        //the ternary is accurate
        assert(dE.getMrXLocation(game) == 1);

        //Round 3
        Move chosenMrXMove = game.getAvailableMoves().asList().get(0);
        game = game.advance(chosenMrXMove);
        game = game.advance(game.getAvailableMoves().asList().get(0));

        BoardHelper.DestinationChecker destCheck = new BoardHelper.DestinationChecker();

        assert(dE.getMrXLocation(game) == destCheck.visit((Move.SingleMove) chosenMrXMove));
    }


}
