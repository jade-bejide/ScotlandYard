package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.Collections.min;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.*;

public class MrXEvaluator extends Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting

    private final List<Double> weights;

    public MrXEvaluator(List<Double> weights){
        this.weights = flatten(weights);
    }

    public List<Double> getWeights() {
        return weights;
    }

    //analyses the distances such that Mr X hyper focuses on the closest detectives to him
    private Integer calcDistanceScore(List<Integer> distances) {
        //compute mean
        int totalSum = 0;

        for (Integer distance : distances) {
            totalSum += distance;
        }

        int n = distances.size();
        int mean = Math.floorDiv(totalSum, n);
        int sumofSqr = 0;
        for (Integer distance : distances) {
            sumofSqr += (int)Math.pow((distance - mean), 2);
        }

        //NOTE: Causes division by zero error when playing against 1 detective!
        try {
            int sd = Math.floorDiv(sumofSqr, (n-1)); //variance
            sd = (int)Math.floor(Math.sqrt(sd));

            Integer closestLocation = min(distances); //get distance of the closest detective
            List<Integer> noOutlierDist = new ArrayList<>();
            noOutlierDist.add(closestLocation);
            distances.remove(closestLocation);

            //only consider statistically close distances (1sd)
            for (Integer distance : distances) {
                if (distance <= closestLocation + (2*sd)) noOutlierDist.add(distance);
            }
            //compute the mean of these values
            int goodSum = 0;

            for (Integer distance : noOutlierDist) {
                goodSum += distance;
            }

            int goodN = noOutlierDist.size();
            return Math.floorDiv(goodSum, goodN);
        } catch (ArithmeticException e) {
            System.out.println("Went wrong here");
            return Math.floorDiv(totalSum, n);
        }
    }

    private int cumulativeDistance(Board.GameState board, Player mrX, List<Player> detectives) {
        int mrXLocation = mrX.location();

        List<Integer> distancePath = new ArrayList<>();
        for (Player detective : detectives) {
            var path = d.shortestPathFromSourceToDestination(mrXLocation, detective, board);
            int distance = path.getFirst();
            distancePath.add(distance);
        }

        if (detectives.size() > 1) return calcDistanceScore(distancePath);
        else return distancePath.get(0);
    }

    private double ticketHeuristic(Board.GameState board) {
        double ticketScore = 0.0;

        if (board.getPlayerTickets(Piece.MrX.MRX).isPresent()) {
            Board.TicketBoard mrXBoard = board.getPlayerTickets(Piece.MrX.MRX).get();

            int taxis = mrXBoard.getCount(TAXI);
            int buses = mrXBoard.getCount(BUS);
            int unders = mrXBoard.getCount(UNDERGROUND);
            int secrets = mrXBoard.getCount(SECRET);
            int doubles = mrXBoard.getCount(DOUBLE);
            int total = taxis + buses + unders + secrets + doubles;

            List<Double> weights = new ArrayList<>();

            weights.add((double) taxis/total);
            weights.add((double) buses/total);
            weights.add((double) unders/total);
            weights.add((double) secrets/total);
            weights.add((double) doubles/total);

            ticketScore += (1-weights.get(0)) * mrXBoard.getCount(TAXI);
            ticketScore += (1-weights.get(1)) * mrXBoard.getCount(BUS);
            ticketScore += (1-weights.get(2)) * mrXBoard.getCount(UNDERGROUND);
            ticketScore += (1-weights.get(3)) * mrXBoard.getCount(SECRET);
            ticketScore += (1-weights.get(4)) * mrXBoard.getCount(DOUBLE);
            return ticketScore;
        }
        return 0.0;
    }

    @Override
    public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
        //after calling minimax, for static evaluation we need to score elements:
        //distance from detectives (tickets away)
        //available moves

        //System.out.println("MrX had moves " + moves);
        System.out.println("MrX is at location: " + (moves.size() > 0 ? moves.get(0).source() : "default"));

        int distance = cumulativeDistance(board, getMrX(board), getDetectives(board));

        int countMoves = moves.size();//board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(Piece.MrX.MRX)).toList().size();

        //or just deduct points for using double and secret tickets
        double score = (weights.get(0) * distance) + (weights.get(1) * countMoves);
        //double ticketScore = ticketHeuristic(board);

        return score;//current score evaluation based on evaluation on distance and moves available
    }


}

