package ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.ui.ai.DoubleTree;
import uk.ac.bris.cs.scotlandyard.ui.ai.Node;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TreeTest {

    @Test public void testPrintTree(){
        DoubleTree tree = new DoubleTree(new Node(0));
        tree.show();
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0))); //should fail until we add nodes
        tree.add(new Node(1));
        tree.add(new Node(2));
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0)));
        tree.add(new Node(3));
        //tree.setLocation(new ArrayList<Integer>(Arrays.asList(0, 0)));
        tree.add(new Node(4));
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(1)));
        tree.add(new Node(10));
        tree.add(new Node(10));
        tree.show();
        Node n = tree.getNodeOnLocation();
        n.show();
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0, 1)));
        n = tree.getNodeOnLocation();
        n.show();
        //assertNotEquals(tree, new DoubleTree(new Node(0)));
    }

    @Test public void testDefaultTree(){
        DoubleTree tree = new DoubleTree(new Node(-1));
        assertTrue(tree.equals(new DoubleTree(new Node(-1))));
    }

    @Test public void testAddToTree(){
        DoubleTree tree = new DoubleTree();
        tree.add(new Node(1));
        tree.add(new Node(2));
        assertEquals(tree, new DoubleTree(new Node(-1, new ArrayList<Node>(
                Arrays.asList(new Node(1), new Node(2))
        ))));
    }
}
