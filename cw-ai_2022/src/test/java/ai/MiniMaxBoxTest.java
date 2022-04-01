package ai;

import org.junit.Assert.*;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
public class MiniMaxBoxTest {

    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;
    private final MiniMaxBox minimax = MiniMaxBox.getInstance(
            new MrXEvaluator(Arrays.asList(0.5, 0.5)),
            new DetectiveEvaluator(Arrays.asList(0.5, 0.5))
    );

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
        Board.GameState state = new MyGameStateFactory().build(new GameSetup(defaultGraph, STANDARD24MOVES),
                mrX, red, green, blue, white, yellow);
        return state;
    }

    @Test public void testTurnsGenerateCorrectly(){
        Board board = getSetup();
        List<Turn> turns = minimax.getTurns(6, (Board.GameState) board); //does just over one loop
        assertThat(turns);
    }
}
