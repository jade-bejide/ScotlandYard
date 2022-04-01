package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public class BoardHelper { //static methods/namespace which holds useful methods relating to the board
    @Nonnull
    public static ImmutableMap<ScotlandYard.Ticket, Integer> getPlayerTickets(Board.GameState board, Piece piece) {
        ArrayList<ScotlandYard.Ticket> ticketTypes = new ArrayList<ScotlandYard.Ticket>(Arrays.asList(TAXI, BUS, UNDERGROUND));

        if (piece.isMrX()) ticketTypes.addAll(Arrays.asList(DOUBLE, SECRET));
        Map<ScotlandYard.Ticket, Integer> tickets = new HashMap<ScotlandYard.Ticket, Integer>();

        for (ScotlandYard.Ticket ticket : ticketTypes) {
            if (board.getPlayerTickets(piece).isPresent()) {
                tickets.put(ticket, board.getPlayerTickets(piece).get().getCount(ticket));
            }
        }

        return ImmutableMap.copyOf(tickets);
    }

    //gets all the players from the board
    @Nonnull
    public static List<Player> getPlayers(Board.GameState board) {
        List<Piece.Detective> detectives = board.getPlayers().stream().filter(Piece::isDetective).map(y -> (Piece.Detective)y).toList();
        List<Piece.MrX> mrXSingle = board.getPlayers().stream().filter(Piece::isMrX).map(y -> (Piece.MrX)y).limit(1).toList();

        List<Piece> pieces = new ArrayList<Piece>();
        pieces.add(mrXSingle.get(0));
        pieces.addAll(detectives);

        List<Player> players = new ArrayList<Player>();

        for (Piece piece : pieces) {
            if (piece.isDetective()) {
                boolean hasLocation = board.getDetectiveLocation((Piece.Detective)piece).isPresent();
                if (hasLocation) {
                    int location = board.getDetectiveLocation((Piece.Detective)piece).get();
                    Player newDetective = new Player(piece, getPlayerTickets(board, piece),location);
                    players.add(newDetective);
                }

            }
            else {
                ImmutableList<LogEntry> log = board.getMrXTravelLog();
                int n = log.size();
                boolean mrXRound = board.getAvailableMoves().stream().anyMatch(x -> x.commencedBy() == Piece.MrX.MRX);
                //default location (no significance)
                int location = 1;
                if (n > 0) {
                    LogEntry lastLog = log.get(n-1);
                    boolean hasLocation = lastLog.location().isPresent();
                    if (hasLocation) {
                        location = lastLog.location().get();
                    }
                } else if (mrXRound) {
                    List<Move> grabMoves = board.getAvailableMoves().stream().limit(1).toList();
                    Move grabMove = grabMoves.get(0);
                    location = grabMove.source();
                }

                Player newMrX = new Player(piece, getPlayerTickets(board, piece), location);
                players.add(newMrX);


            }
        }

        return players;
    }

    //returns all detectives on the board, as a list of players
    public static List<Player> getDetectives(Board.GameState board) {
        return getPlayers(board).stream().filter(Player::isDetective).toList();
    }

    public static List<Piece> getRemaining(Board.GameState board) {
        Set<Piece> players = new HashSet<Piece>();
        for(Move move : board.getAvailableMoves()){
            players.add(move.commencedBy());
        }
        return new ArrayList<Piece>(players);
    }

    //get a singular detective
    public static Player getDetectiveOnPiece(Board.GameState board, Piece piece) {
        return getDetectives(board).stream().filter(x -> x.piece().equals(piece)).toList().get(0);
    }

    //returns mr X from the board as a player
    public static Player getMrX(Board.GameState board) {
        List<Player> mrXS = getPlayers(board).stream().filter(Player::isMrX).toList();
        return mrXS.get(0);
    }
}
