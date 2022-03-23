package ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.MyAi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 138);
        Board.GameState state = new MyGameStateFactory().build(new GameSetup(defaultGraph, STANDARD24MOVES),
                mrX, red, green, blue, white, yellow);

        Ai ai = new MyAi();

        test1(ai, state);
    }

    private static void test1(Ai ai, Board.GameState state){
        System.out.println("hello");
    }
}