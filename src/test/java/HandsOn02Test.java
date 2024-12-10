import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;

public class HandsOn02Test {
    @Resource
    private MemberBhv memberBhv;

    public void test_existsTestData() throws Exception {
        // ## Arrange ##
        // ## Act ##

        // ## Assert ##
        assertTrue(1 > 0);
    }
}
