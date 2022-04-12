package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.min;
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

    private Player getMrX(Board.GameState board) {
        List<Player> mrXS = getPlayers(board).stream().filter(Player::isMrX).toList();
        return mrXS.get(0);
    }

    //analyses the distances such that Mr X hyperfocuses on the closest detectives to him
    private Integer calcDistanceScore(List<Integer> distances) {
        //compute mean
        int totalSum = distances.stream().mapToInt(x -> x).sum();
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

            Integer closestLocation = min(distances); //get distance of closest detective
            List<Integer> noOutlierDist = new ArrayList<>();
            noOutlierDist.add(closestLocation);
            distances.remove(closestLocation);

            //only consider statistically close distances (1sd)
            for (Integer distance : distances) {
                if (distance <= closestLocation + sd) noOutlierDist.add(distance);
            }

            //compute the mean of these values
            int goodSum = noOutlierDist.stream().mapToInt(x -> x).sum();
            int goodN = noOutlierDist.size();
            return Math.floorDiv(goodSum, goodN);
        } catch (ArithmeticException e) {
            return Math.floorDiv(totalSum, n);
        }
    }

    private int cumulativeDistance(Board.GameState board, Player mrX, List<Player> detectives) {
        Integer mrXLocation = mrX.location();
        List<Integer> distancePath = new ArrayList<>();
        for (Player detective : detectives) {
            var path = d.shortestPathFromSourceToDestination(mrXLocation, detective, board);
            int distance = path.getFirst();
            distancePath.add(distance);
            List<Integer> nodes = path.getMiddle(); //may want to use for whatever reason
            List<ScotlandYard.Ticket> ticketUsed = path.getLast(); //for testing, assert that detective had enough tickets to travel that path

        }

        if (detectives.size() > 1) return calcDistanceScore(distancePath);
        else return distancePath.get(0);
    }

    @Override
    public double score(Piece inPlay, List<Move> moves, Board.GameState board) {
        //after calling minimax, for static evaluation we need to score elements:
        //distance from detectives (tickets away)
        //available moves
        int distance = cumulativeDistance(board, getMrX(board), getDetectives(board));

        int countMoves = moves.size();//board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(Piece.MrX.MRX)).toList().size();

        return (weights.get(0) * distance) + (weights.get(1) * countMoves);//current score evaluation based on evaluation on distance and moves available
    }


}

