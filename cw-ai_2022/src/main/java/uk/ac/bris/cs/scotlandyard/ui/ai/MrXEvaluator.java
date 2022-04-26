package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;
import java.util.stream.Collectors;
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

            int buses = mrXBoard.getCount(BUS);
            int unders = mrXBoard.getCount(UNDERGROUND);
            int secrets = mrXBoard.getCount(SECRET);
            int doubles = mrXBoard.getCount(DOUBLE);
            List<Integer> values = Arrays.asList(buses, unders, secrets, doubles);
            int total = buses + unders + secrets + doubles;

            //checking if there's one set of tickets left
            int zeroTickets = (int)values.stream().filter(x -> x == 0).count();
            if (values.size() - zeroTickets == 1) {
                return values.stream().filter(x -> x != 0).toList().get(0);
            }

            //mrx may have no tickets in some minimax tests
            //please message elliot about removing/working around this if needed
            if(total == 0) { return 0; }

            List<Double> weights = new ArrayList<>();

            weights.add((double) buses/total);
            weights.add((double) unders/total);
            weights.add((double) secrets/total);
            weights.add((double) doubles/total);
            //ignore since tickets are likely to bus used alot anyway
            //ticketScore += (1-weights.get(0)) * mrXBoard.getCount(TAXI);
            ticketScore += (1-weights.get(0)) * mrXBoard.getCount(BUS);
            ticketScore += (1-weights.get(1)) * mrXBoard.getCount(UNDERGROUND);
            ticketScore += (1-weights.get(2)) * mrXBoard.getCount(SECRET);
            ticketScore += (1-weights.get(3)) * mrXBoard.getCount(DOUBLE);
            return ticketScore;
        }
        return 0.0;
    }



    public int getSafeMoves(List<Move> moves, Board.GameState board, Player mrX) {

        DestinationChecker safeMoves = new DestinationChecker();

        Board.GameState nxtBoard = board.advance(moves.get(0));

        List<Move> detectiveMoves = List.copyOf(nxtBoard.getAvailableMoves());

        ImmutableList<Integer> detectivePossibleLocations = ImmutableList.copyOf(detectiveMoves.stream().map(x -> x.accept(safeMoves)).toList());
        ImmutableList<Integer> mrXPossibleLocations = ImmutableList.copyOf(board.getAvailableMoves().stream().map(x -> x.accept(safeMoves)).toList());

        return moves.size() + (int) mrXPossibleLocations.stream().filter(x -> !detectivePossibleLocations.contains(x)).count();
    }

    private int getMrXLocation(List<Move> moves, int id){
        DestinationChecker destinationVisitor = new DestinationChecker();
        // returns mrx's location at parent/this node being evaluated (his actual location)
        // ...i.e. if its not his turn when evaluator is called, it will retrieve where he was was
        return moves.get(id).accept(destinationVisitor);
    }

    @Override
    public double score(Piece inPlay, List<Move> moves, int id, Board.GameState board) {
        //after calling minimax, for static evaluation we need to score elements:
        //distance from detectives (tickets away)
        //available moves

        int mrXLocation = -1;
        if (moves.size() > 0) {
            if (!(moves.stream().allMatch(x -> x.commencedBy().isMrX()))) {
                throw new IllegalArgumentException("All moves should be commenced by Mr X!");
            }
            mrXLocation = moves.get(0).source();
            if(id != -1) {
                mrXLocation = getMrXLocation(moves, id);
            }
        }

        if (mrXLocation == -1) {
            Random random = new Random();
            mrXLocation = ScotlandYard.MRX_LOCATIONS.get(random.nextInt(ScotlandYard.MRX_LOCATIONS.size()));
        }

        int distance = cumulativeDistance(board, getMrX(board, mrXLocation), getDetectives(board));
        //current score evaluation based on evaluation on distance and moves available and tickets
        return (weights.get(0) * distance) + (weights.get(1) * getSafeMoves(moves, board, getMrX(board, mrXLocation))) +
                (weights.size() == 3 ? (weights.get(2) * ticketHeuristic(board)) : 0); //incase we have only two weights like in jade's mrxEvaluator tests
    }


}

