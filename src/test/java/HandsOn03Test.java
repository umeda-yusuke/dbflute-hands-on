import static org.dbflute.util.DfTypeUtil.toLocalDate;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn03Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    public void test_会員名称がSで始まる1968年1月1日以前に生まれた会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberName_LikeSearch("S", op -> op.likePrefix());
            cb.query().setBirthdate_LessEqual(toLocalDate("1968-01-01"));
        });
        // ## Assert ##
        members.forEach(member -> {
            assertTrue(member.getMemberName().startsWith("S"));
            assertTrue(member.getBirthdate().isBefore(toLocalDate("1968-01-02")));
        });
    }

    public void test_会員ステータスと会員セキュリティ情報も取得して会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberStatus();
            cb.setupSelect_MemberSecurityAsOne();
        });
        // ## Assert ##
        members.forEach(member -> {
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        });
    }

    public void test_会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        // ## Assert ##
        members.forEach(member -> {
            assertTrue(member.getMemberSecurityAsOne().get().getReminderQuestion().contains("2"));
        });
    }

    public void test_会員ステータスの表示順カラムで会員を並べて検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().addOrderBy_MemberStatusCode_Asc();
        });
        // ## Assert ##
        for (int i = 0; i < members.size() - 1; i++) {
            String currentStatusCode = members.get(i).getMemberStatusCode();
            String nextStatusCode = members.get(i + 1).getMemberStatusCode();
            assertTrue(currentStatusCode.compareTo(nextStatusCode) <= 0);
        }
    }

    public void test_生年月日が存在する会員の購入を検索する() {
        // ## Arrange ##
        // ## Act ##
        // ## Assert ##
    }
}
