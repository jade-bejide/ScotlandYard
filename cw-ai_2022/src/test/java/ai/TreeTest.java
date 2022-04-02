package ai;

import com.sun.source.tree.Tree;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.ui.ai.DoubleTree;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeTest {

    @Test public void testAddTree(){
        DoubleTree t = new DoubleTree(0,
                Arrays.asList(
                        new DoubleTree.Node(1, new ArrayList<DoubleTree.Node>(
                                Arrays.asList(
                                        new DoubleTree.Node(3, new ArrayList<DoubleTree.Node>()),
                                        new DoubleTree.Node(4, new ArrayList<DoubleTree.Node>())
                                )
                        )),
                        new DoubleTree.Node(2, new ArrayList<DoubleTree.Node>())
                ));

        System.out.println(t);
    }
}
