package ai;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultDetectiveTickets;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.defaultMrXTickets;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.*;

public class BoardHelperTest extends RenameMe {

    //checks that the players are rebuilt correctly
    @Test
    public void testGetPlayersRebuildsPlayersCorrectly() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 22);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 5);
        Player green = new Player(Piece.Detective.GREEN, defaultDetectiveTickets(), 101);
        Player red = new Player (Piece.Detective.RED, defaultDetectiveTickets(), 17);
        Player yellow = new Player(Piece.Detective.YELLOW, defaultDetectiveTickets(), 45);
        Player white = new Player(Piece.Detective.WHITE, defaultDetectiveTickets(), 98);
        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, green, blue, red, yellow, white);

        List<Player> boardHelperPlayers = getPlayers(game);
        ImmutableSet<Piece> boardPlayers = game.getPlayers();

        for (Player player : boardHelperPlayers) {
            assert(boardPlayers.contains(player.piece()));
            if (game.getPlayerTickets(player.piece()).isPresent()) {
                Board.TicketBoard boardTickets = game.getPlayerTickets(player.piece()).get();
                ImmutableMap<ScotlandYard.Ticket, Integer> boardHelperTicket = getPlayerTickets(game, player.piece());

                for(Map.Entry<ScotlandYard.Ticket, Integer> ticket : boardHelperTicket.entrySet()) {
                    assert(boardTickets.getCount(ticket.getKey()) == boardHelperTicket.get(ticket.getKey()));
                }

                if (player.isMrX()) assert(player.location() == 22);
                else {
                    if (game.getDetectiveLocation((Piece.Detective) player.piece()).isPresent()) {
                        assert(player.location() == game.getDetectiveLocation((Piece.Detective) player.piece()).get());
                    }
                }
            }
        }
    }

    @Test
    public void testThatGameAlwaysContainsMrXAndAtLeastOneDetective() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 22);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 5);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue);

        Player boardHelperMrX = getMrX(game);
        List<Player> detectives = getDetectives(game);

        assert(detectives.get(0).piece() == BLUE);
        assert(boardHelperMrX != null);

        assert(getPlayers(game).containsAll(detectives) && getPlayers(game).contains(boardHelperMrX));
    }

    @Test
    public void testThatDetectivesDoesNotContainsMrX() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 22);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 5);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 76);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, green);

        List<Player> detectives = getDetectives(game);
        assert(detectives.stream().noneMatch(x -> x.isMrX()));
    }

    @Test
    public void testThatGetMrXReturnsMrX() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 22);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 5);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 76);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, green);

        assert(getMrX(game).isMrX());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDetectiveOnPieceDoesNotTakeMrX() {
        Player mrX = new Player(Piece.MrX.MRX, defaultMrXTickets(), 22);
        Player blue = new Player(BLUE, defaultDetectiveTickets(), 5);
        Player green = new Player(GREEN, defaultDetectiveTickets(), 76);

        Board.GameState game = new MyGameStateFactory().build(standard24MoveSetup(), mrX, blue, green);

        getDetectiveOnPiece(game, MrX.MRX);
    }
}
