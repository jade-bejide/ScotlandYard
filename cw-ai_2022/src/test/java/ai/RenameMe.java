package ai;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.checkerframework.checker.index.qual.NonNegative;
import org.junit.BeforeClass;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public class RenameMe {
    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;
    @BeforeClass
    public static void setUp() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (
                IOException e) { throw new RuntimeException("Unable to read game graph", e); }
    }

    @Nonnull
    static GameSetup standard24MoveSetup() {
        setUp();
        return new GameSetup(defaultGraph, STANDARD24MOVES);
    }

    @Nonnull static ImmutableMap<Ticket, Integer> makeTickets(
            int taxi, int bus, int underground, int x2, int secret) {
        return ImmutableMap.of(
                TAXI, taxi,
                BUS, bus,
                UNDERGROUND, underground,
                Ticket.DOUBLE, x2,
                Ticket.SECRET, secret);
    }
}
