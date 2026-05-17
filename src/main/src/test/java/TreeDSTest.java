package test.java;
import main.java.DS.TreeDS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TreeDSTest {
    TreeDS<Integer> treeDS;
    @BeforeEach
    public void prepare(){
        treeDS = new TreeDS<>();
    }

    @Test
    public void testCreate(){
        treeDS.addSet(1);
        treeDS.addSet(2);
        treeDS.addSet(3);
        assertEquals(3, treeDS.getSetCount());
    }

    @Test
    public void testUnionSize(){
        treeDS.addSet(1);
        treeDS.addSet(2);
        treeDS.addSet(3);

        treeDS.unionSets(1,2);
        treeDS.unionSets(1, 3);
        assertEquals(1, treeDS.getSetCount());
    }

    @Test
    public void testUnionParent(){
        treeDS.addSet(1);
        treeDS.addSet(2);
        treeDS.addSet(3);

        treeDS.unionSets(1,2);
        assertEquals(1, treeDS.getSet(2));
    }
}
