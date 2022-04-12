package ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.DetectiveEvaluator;

import java.util.Arrays;
import java.util.HashSet;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;


public class DetectivesEvaluatorTest extends RenameMe {
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
        dE.setMrXBoundary(176, game);

        assert(dE.getMrXBoundary().stream().mapToInt(x -> x).sum() != 19900);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoIllegalRevealedLocationAllowed() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(-2, game);
    }

    //will test that set boundary matches expected
    @Test
    public void testBoundaryUpdatesCorrectly() {
        Player mrX = new Player(MrX.MRX, defaultMrXTickets(), 54);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 199);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        DetectiveEvaluator dE = new DetectiveEvaluator(Arrays.asList(1.0, 1.0));
        dE.setMrXBoundary(18, game);
    }

    //will test that set boundary matches expected given needed filtering
    @Test
    public void testBoundaryFilteredCorrectly() {

    }
}
