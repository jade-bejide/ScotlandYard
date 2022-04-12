package ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.ui.ai.DoubleTree;
import uk.ac.bris.cs.scotlandyard.ui.ai.Node;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TreeTest {

    @Test (expected = IndexOutOfBoundsException.class)
    public void testIllegalSetLocation(){ //trying to index children that dont exist
        DoubleTree tree = new DoubleTree();
        tree.setLocation(new ArrayList<Integer>(Arrays.asList(0)));
    }

    @Test
    public void testTreesAreEqual(){
        DoubleTree tree1 = new DoubleTree();
        DoubleTree tree2 = new DoubleTree();
        assert(tree1.equals(tree2));
    }

    @Test
    public void testTreesArentEqual(){
        DoubleTree tree1 = new DoubleTree(new Node(0,
                new ArrayList<Node>(Arrays.asList(new Node(-1,
                        new ArrayList<Node>(Arrays.asList(new Node(-3), new Node(-2)))),
                new Node(1,
                        new ArrayList<Node>(Arrays.asList(new Node(2), new Node(3))))))
        ));
        DoubleTree tree2 = new DoubleTree(new Node(0,
                new ArrayList<Node>(Arrays.asList(new Node(-1,
                                new ArrayList<Node>(Arrays.asList(new Node(-4), new Node(-2)))),
                        new Node(1,
                                new ArrayList<Node>(Arrays.asList(new Node(2))))))
        ));
        assertFalse(tree1.equals(tree2));
    }

    @Test public void testDefaultTree(){
        DoubleTree tree = new DoubleTree();
        assert(tree.equals(new DoubleTree(new Node(-1))));
    }

    @Test public void testAddToTree(){
        DoubleTree tree1 = new DoubleTree(new Node(1));
        tree1.addNodeOnLocation(new Node(1));
        tree1.addNodeOnLocation(new Node(2));
        DoubleTree tree2 = new DoubleTree(new Node(1,
                new ArrayList<Node>(Arrays.asList(new Node(1), new Node(2))
        )));
        assert(tree1.equals(tree2));
    }

//    @Test public void testAddToTreeSanity(){
//        DoubleTree tree = new DoubleTree(new Node(1));
//        tree.show();
//    }
}
