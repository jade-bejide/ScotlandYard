package ai;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BoardHelperTest.class,
        ShortestPathTest.class,
        MrXEvaluatorTest.class,
        DetectivesEvaluatorTest.class
        MiniMaxBoxTest.class,
        TreeTest.class
})
public class AllTest {

}
