
package ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import io.atlassian.fugue.Pair;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

//Dijkstra Algorithm Related Tests
public class ShortestPathTest extends RenameMe {
    @Test
    public void testDetectivePathContainsNoIllegalTickets() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 32);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 54);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 34);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 21);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, red, yellow, white);


        Dijkstra dijk = new Dijkstra();
        List<Player> detectives = Arrays.asList(blue, green, red, yellow, white);

        for (Player detective : detectives) {
            dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game);
            NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> dijks = dijk.shortestPathFromSourceToDestination(mrX.location(), detective, game);
            assertFalse(dijks.getLast().stream().anyMatch(x -> x == SECRET || x == DOUBLE));
        }

    }

    //There are 199 nodes
    @Test
    public void testNodesPopulatedCorrectly() {
        Dijkstra dijk = new Dijkstra();

        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 199);


        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green);

        //make proxy games enabling the dict to be populated
        dijk.shortestPathFromSourceToDestination(mrX.location(), green, game);
        for (Integer n : game.getSetup().graph.nodes()) {
            assertTrue(dijk.getPopulatedDict().get(n) != null);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDijkstraDoesntAttemptIllegalNodes() {
        Dijkstra dijk = new Dijkstra();

        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 198);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 32);
        Player purple = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 300);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);
        dijk.shortestPathFromSourceToDestination(0, blue, game);
        dijk.shortestPathFromSourceToDestination(mrX.location(), purple, game);
    }

    @Test
    public void testDijkstraReachesDestination() {
        Dijkstra dijk = new Dijkstra();

        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 1);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 178);
        Player red = new Player(Piece.Detective.RED, defaultDetectiveTickets(), 24);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 83);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, red, yellow);

        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> bluePath = dijk.shortestPathFromSourceToDestination(mrX.location(), blue, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> redPath = dijk.shortestPathFromSourceToDestination(mrX.location(), red, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> yellowPath = dijk.shortestPathFromSourceToDestination(mrX.location(), yellow, game);

        List<List<Integer>> paths = Arrays.asList(bluePath.getMiddle(), redPath.getMiddle(), yellowPath.getMiddle());

        for (List<Integer> path : paths) {
            int n = path.size();
            assert(path.get(n-1) == mrX.location());
        }

    }

    @Test
    public void testShortestPathAchieved() {
        Dijkstra dijk = new Dijkstra();

        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 141);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 176);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 47);
        Player red = new Player(Piece.Detective.RED, defaultDetectiveTickets(), 78);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, yellow, red);

        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> redPath = dijk.shortestPathFromSourceToDestination(mrX.location(), red, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> yellowPath = dijk.shortestPathFromSourceToDestination(mrX.location(), yellow, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> greenPath = dijk.shortestPathFromSourceToDestination(mrX.location(), green, game);

        //Assert that the distance is the shortest path
        assert(redPath.getFirst() == 6);
        assert(yellowPath.getFirst() == 6);
        assert(greenPath.getFirst() == 5);

        //.size() - 1 to account for its start position
        //Assert that the number of nodes travelled is equal to the distance
        assert(redPath.getMiddle().size()-1 == redPath.getFirst());
        assert(yellowPath.getMiddle().size()-1 == yellowPath.getFirst());
        assert(greenPath.getMiddle().size()-1 == greenPath.getFirst());



        //Assert the number of nodes travelled is equal to the number of tickets used
        assert(redPath.getMiddle().size()-1 == redPath.getLast().size());
        assert(yellowPath.getMiddle().size()-1 == yellowPath.getLast().size());
        assert(greenPath.getMiddle().size()-1 == greenPath.getLast().size());
    }

    @Test
    public void testPathTravelledReturnsCorrespondingTickets() {
        Dijkstra dijk = new Dijkstra();

        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 199);
        Player blue = new Player(Piece.Detective.BLUE, defaultDetectiveTickets(), 3);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 51);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 84);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, white , green);

        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> bluePath = dijk.shortestPathFromSourceToDestination(mrX.location(), blue, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> whitePath = dijk.shortestPathFromSourceToDestination(mrX.location(), white, game);
        NdTypes.Triple<Integer, List<Integer>, List<ScotlandYard.Ticket>> greenPath = dijk.shortestPathFromSourceToDestination(mrX.location(), green, game);

        assert(assertThatPathMatchesTickets(bluePath.getMiddle(), bluePath.getLast(), game));
        assert(assertThatPathMatchesTickets(whitePath.getMiddle(), whitePath.getLast(), game));
        assert(assertThatPathMatchesTickets(greenPath.getMiddle(), greenPath.getLast(), game));
    }

    private boolean assertThatPathMatchesTickets(List<Integer> path, List<ScotlandYard.Ticket> ticketsUsed, Board.GameState board) {
        int i = 0;

        while (i < path.size()-1) {
            if (board.getSetup().graph.edgeValue(path.get(i), path.get(i+1)).isPresent()) {
                Ticket ticket1 = board.getSetup().graph.edgeValue(path.get(i), path.get(i+1)).get().stream().toList().get(0).requiredTicket();
                Ticket ticket2 = ticketsUsed.get(i);
                if (!ticket1.equals(ticket2)) return false;
                i += 1;
            } else return false;
        }
        return true;
    }

    //public test search list


}
