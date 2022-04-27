package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.model.PlayerProperty;

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
            } else {
                tickets.put(ticket, 0);
            }
        }

        return ImmutableMap.copyOf(tickets);
    }

    private static boolean checkPlayersAlwaysContainsDetectiveOrMrX(List<Player> players, boolean ifMrX) {
        boolean playerPredicate = false;
        for (Player player : players) {
            if (!ifMrX) {
                if (player.isDetective()) return true;
            }
            else {
                if (player.isMrX()) return true;
            }
        }

        return playerPredicate;
    }

    //gets all the players from the board
    @Nonnull
    public static List<Player> getPlayers(Board.GameState board) {
        List<Player> players = new ArrayList<Player>();

        for (Piece piece : board.getPlayers()) {
            if (piece.isDetective()) {
                boolean hasLocation = board.getDetectiveLocation((Piece.Detective)piece).isPresent();
                if (hasLocation) {
                    int location = board.getDetectiveLocation((Piece.Detective)piece).get();
                    Player newDetective = new Player(piece, getPlayerTickets(board, piece),location);
                    players.add(newDetective);
                }

            } else {
                ImmutableList<LogEntry> log = board.getMrXTravelLog();
                int n = log.size();
                boolean mrXRound = board.getAvailableMoves().stream().anyMatch(x -> x.commencedBy().equals(Piece.MrX.MRX));
                //default location (no significance)
                int location = 1;
                if (n > 0) {
                    LogEntry lastLog = log.get(n-1);
                    boolean hasLocation = lastLog.location().isPresent();
                    if (hasLocation) {
                        location = lastLog.location().get();
                    }
                }
                if (mrXRound) {
                    Move grabMove = board.getAvailableMoves().asList().get(0);
                    location = grabMove.source();
                }

                Player newMrX = new Player(Piece.MrX.MRX, getPlayerTickets(board, Piece.MrX.MRX), location);
                players.add(newMrX);
            }
        }


        if (!checkPlayersAlwaysContainsDetectiveOrMrX(players, false)) throw new IllegalArgumentException("A game must always contain detectives!");
        if (!checkPlayersAlwaysContainsDetectiveOrMrX(players, true)) throw new IllegalArgumentException("A game must always contains mr X!");
        return players;
    }

    //returns all detectives on the board, as a list of players
    public static List<Player> getDetectives(Board.GameState board) {
        return getPlayers(board).stream().filter(Player::isDetective).toList();
    }

    //returns the remaining pieces
    public static List<Piece> getRemaining(Board.GameState board) {
        Set<Piece> players = new HashSet<Piece>();
        for(Move move : board.getAvailableMoves()){
            players.add(move.commencedBy());
        }
        return new ArrayList<Piece>(players);
    }

//    public static Player getPlayerOnPiece(Board.GameState board, Piece piece) {
//        List<Player> players = new ArrayList<Player>();
//        try { players.add(getDetectiveOnPiece(board, piece)); }
//        catch (IndexOutOfBoundsException e) { System.out.println("Warning: No detectives?, " + e); }
//        try { players.add(getMrX(board)); }
//        catch (IndexOutOfBoundsException e) { System.out.println("Warning: No MRX?, " + e); }
//        return players.get(0);
//    }

    //get a singular detective
    public static Player getDetectiveOnPiece(Board.GameState board, Piece piece) {
        //System.out.println(getDetectives(board) + "\n and piece: " + piece);
        for (Player detective : getDetectives(board)) {
            if (detective.piece() == piece) return detective;
        }

        throw new IllegalArgumentException("Method wasn't handed a detective!");
    }

    //returns mr X from the board as a player
    public static Player getMrX(Board.GameState board, int currentLocation) {
        Player mrX = getPlayers(board).get(0);
        mrX = mrX.at(currentLocation);
        return mrX;
    }

//    public int getMrXLocation(Board.GameState board) {
//        //take a snapshot
//        ImmutableBoard savedBoard = new ImmutableBoard(board);
//        Player fakeMrX = new Player(Piece.MrX.MRX, getPlayerTickets(board, Piece.MrX.MRX), 1);
//        Board.GameState moveBoard = MyGameStateFactory.build(
//                savedBoard.getSetup(),
//        )
//    }

    static class DestinationChecker implements Move.Visitor<Integer> {

        @Override
        public Integer visit(Move.SingleMove move) {
            return move.destination;
        }

        //Note that this will never be called
        @Override
        public Integer visit(Move.DoubleMove move) {
            return move.destination2;
        }
    }

    public List<Integer> getPlayerPossibleLocations(Board.GameState board, Piece piece) {
        Set<Integer> posNodes = new HashSet<>();

        if (piece.isDetective()) {
            Player detective = getDetectiveOnPiece(board, piece);
            posNodes.addAll(board.getSetup().graph.adjacentNodes(detective.location()));
            Set<Integer> posNodesCpy = new HashSet<Integer>();
            if (board.getPlayerTickets(piece).isPresent()) {
                Board.TicketBoard tickets = board.getPlayerTickets(piece).get();

                for (Integer pos : posNodes) {
                    if (board.getSetup().graph.edgeValue(detective.location(), pos).isPresent()) {
                        List<ScotlandYard.Transport> neededTransport = List.copyOf(board.getSetup().graph.edgeValue(detective.location(), pos).get());

                        boolean canTravel = neededTransport.stream().anyMatch(x -> tickets.getCount(x.requiredTicket()) > 0);

                        if (canTravel) posNodesCpy.add(pos);
                    }
                }

                return List.copyOf(posNodesCpy);
            }

        } else {
            //get mr X adjacent nodes
        }

        return List.of();
    }

    public static void printLog(Board.GameState board) {
        ImmutableList<LogEntry> log = board.getMrXTravelLog();
        int n = log.size();

        for (LogEntry logEntry : log) {
            System.out.println(logEntry.location());
        }
    }

    public static LogEntry getLastLog(Board.GameState board) {
        ImmutableList<LogEntry> log = board.getMrXTravelLog();
        int n = log.size();

        return log.get(n-1);
    }
}
