package ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.ui.ai.DoubleTree;
import uk.ac.bris.cs.scotlandyard.ui.ai.Node;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeTest {

    @Test public void testAddTree(){
        DoubleTree tree = new DoubleTree(new Node(0));
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0)));
        tree.addAsChildOfLocation(new Node(1));
        tree.addAsChildOfLocation(new Node(2));
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0)));
        tree.addAsChildOfLocation(new Node(3));
        //tree.setLocation(new ArrayList<Integer>(Arrays.asList(0, 0)));
        tree.addAsChildOfLocation(new Node(4));
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(1)));
        tree.addAsChildOfLocation(new Node(10));
        tree.show();
    }
}
