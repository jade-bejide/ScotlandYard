package ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

public class MrXEvaluatorPrivateMethodsBase {
    public double ticketHeuristic(Board.GameState board) {
        double ticketScore = 0.0;

        if (board.getPlayerTickets(Piece.MrX.MRX).isPresent()) {
            Board.TicketBoard mrXBoard = board.getPlayerTickets(Piece.MrX.MRX).get();

            int taxis = mrXBoard.getCount(TAXI);
            int buses = mrXBoard.getCount(BUS);
            int unders = mrXBoard.getCount(UNDERGROUND);
            int secrets = mrXBoard.getCount(SECRET);
            int doubles = mrXBoard.getCount(DOUBLE);
            int total = taxis + buses + unders + secrets + doubles;

            //mrx may have no tickets in some minimax tests
            //please message elliot about removing/working around this if needed
            if(total == 0) { return 0; }

            List<Double> weights = new ArrayList<>();

            weights.add((double) taxis/total);
            weights.add((double) buses/total);
            weights.add((double) unders/total);
            weights.add((double) secrets/total);
            weights.add((double) doubles/total);

            //ignore since tickets are likely to bus used alot anyway
            //ticketScore += (1-weights.get(0)) * mrXBoard.getCount(TAXI);
            ticketScore += (1-weights.get(1)) * mrXBoard.getCount(BUS);
            ticketScore += (1-weights.get(2)) * mrXBoard.getCount(UNDERGROUND);
            ticketScore += (1-weights.get(3)) * mrXBoard.getCount(SECRET);
            ticketScore += (1-weights.get(4)) * mrXBoard.getCount(DOUBLE);
            return ticketScore;
        }
        return 0.0;
    }
}
