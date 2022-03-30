package ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.Cy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class MiniMax{
    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;

    public static void main(String[] args){
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

        Ai ai = new Cy();

        //testFromMrX(ai, state); //can we predict for mrX
        testMultipleMoves(ai, state);
    }

    private static void testFromMrX(Ai ai, Board.GameState state) {
        long time = System.nanoTime();
        System.out.println(ai.pickMove(state, new Pair<Long, TimeUnit>(15L, TimeUnit.MILLISECONDS)));
        System.out.println(((System.nanoTime() - time) / 1000000) + "ms");
    }

    private static void testMultipleMoves(Ai ai, Board.GameState state) {
        final int turns = 4;
        for(int i = 0; i < turns; i++){
            long time = System.nanoTime();
            Move move = ai.pickMove(state, new Pair<Long, TimeUnit>(15L, TimeUnit.MILLISECONDS));
            System.out.println(move);
            state = state.advance(move);
            System.out.println(((System.nanoTime() - time) / 1000000) + "ms");
        }
    }
}