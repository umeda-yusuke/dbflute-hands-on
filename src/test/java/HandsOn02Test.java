import java.util.List;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {

    @Resource
    private MemberBhv memberBhv;

    public void test_会員名称がSで始まる会員を取得する() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
        });
        // ## Assert ##
        members.forEach(member -> {
            assertTrue(member.getMemberName().startsWith("S"));
        });
    }

    public void test_会員IDが1の会員を取得する() throws Exception {
        // ## Arrange ##
        // ## Act ##
        OptionalEntity<Member> member = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(1);
        });
        // ## Assert ##
        assertEquals(1, (int) member.get().getMemberId());
    }

    public void test_生年月日がない会員を取得する() throws Exception {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setBirthdate_IsNull();
        });
        // ## Assert ##
        members.forEach(member -> {
            assertNull(member.getBirthdate());
        });
    }
}
