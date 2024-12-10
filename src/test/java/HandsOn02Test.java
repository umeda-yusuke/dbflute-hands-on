import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test_existsTestData() throws Exception {
        // ## Arrange ##
        // ## Act ##

        // ## Assert ##
        assertTrue(1 > 0);
    }
}
