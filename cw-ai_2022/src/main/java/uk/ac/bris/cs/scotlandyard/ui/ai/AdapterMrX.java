package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.min;
import static uk.ac.bris.cs.scotlandyard.ui.ai.BoardHelper.*;

public class AdapterMrX implements Evaluator{
    private final Dijkstra d = new Dijkstra(); //what we're adapting

    private final List<Integer> weights;

    AdapterMrX(List<Integer> weights){
        this.weights = weights;
    }

    private Player getMrX(Board.GameState board) {
        List<Player> mrXS = getPlayers(board).stream().filter(Player::isMrX).toList();
        return mrXS.get(0);
    }

    private Integer calcDistanceScore(List<Integer> distances) {
        //compute mean
        int totalSum = distances.stream().mapToInt(x -> x.intValue()).sum();
        int n = distances.size();
        int mean = Math.floorDiv(totalSum, n);
        int sumofSqr = 0;
        for (Integer distance : distances) {
            sumofSqr += (int)Math.pow((distance - mean), 2);
        }

        int sd = (int)Math.floorDiv(sumofSqr, (n-1)); //standard deviation

        Integer closestLocation = min(distances); //get distance of closest detective
        List<Integer> noOutlierDist = new ArrayList<>();
        noOutlierDist.add(closestLocation);
        distances.remove(closestLocation);

        //only consider statistically close distances (1sd)
        for (Integer distance : distances) {
            if (distance <= closestLocation + sd) noOutlierDist.add(distance);
        }

        //compute the mean of these values
        int goodSum = noOutlierDist.stream().mapToInt(x -> x.intValue()).sum();
        int goodN = noOutlierDist.size();



        return (int)Math.floorDiv(goodSum, goodN);
    }

    private int cumulativeDistance(Board.GameState board, Player mrX, List<Player> detectives) {
        int min = Integer.MAX_VALUE;
        Integer mrXLocation = mrX.location();
        List<Integer> distancePath = new ArrayList<>();
        int totalDistance = 0;
        for (Player detective : detectives) {
            Integer detectiveLocation = detective.location();
            var path = d.shortestPathFromSourceToDestination(board.getSetup().graph, detectiveLocation, mrXLocation, detective, board);
            int distance = path.getFirst();
            //System.out.println(distance);
            distancePath.add(distance);
            totalDistance += (int)Math.sqrt(distance);
            List<Integer> nodes = path.getMiddle(); //may want to use for whatever reason
            List<ScotlandYard.Ticket> ticketUsed = path.getLast(); //for testing, assert that detective had enough tickets to travel that path

        }

        //distancePath.sort((x, y) -> x - y);

        return calcDistanceScore(distancePath);
    }



    public int score(Board.GameState board) {
        //after calling minimax, for static evaluation we need to score elements:
        //distance from detectives (tickets away)
        //available moves
        int distance = cumulativeDistance(board, getMrX(board), getDetectives(board));
        System.out.println(distance);
        int countMoves = board.getAvailableMoves().stream().filter(x -> x.commencedBy().equals(Piece.MrX.MRX)).toList().size();

        return (int)Math.floor(0.7 * distance + 0.3 * countMoves);//current score evaluation based on evaluation on distance and moves available
    }
}

