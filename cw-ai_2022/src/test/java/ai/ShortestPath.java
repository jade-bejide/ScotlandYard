
package ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MyAi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class ShortestPath {
    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;
    public static void main(String[] args) {

        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (
                IOException e) { throw new RuntimeException("Unable to read game graph", e); }


        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 32);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 54);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 34);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 21);
        Board.GameState game = new MyGameStateFactory().build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, green, red, yellow, white);

        Pair<Long, TimeUnit> time = new Pair<Long, TimeUnit>(15L, SECONDS);

        long start = System.nanoTime();
        MyAi ai = new MyAi();
        System.out.println(ai.score(game));
        long end = System.nanoTime();
        System.out.println("Approximate Time Taken: " + (end-start));
    }
}
