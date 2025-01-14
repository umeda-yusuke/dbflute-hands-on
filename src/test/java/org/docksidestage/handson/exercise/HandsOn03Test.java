package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done umeyan 不要なimport文の削除をお願い by jflute (2025/01/07)
// done umeyan セクション2と同じようにjavadocお願い by jflute (2025/01/07)
// done umeyan testクラスのpackageがdefault packageになっちゃってるので、要件通りの場所に移動してください by jflute (2025/01/07)

/**
 * The test of hands-on for basic select. (simple cases)
 * @author umeyan
 */
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
        assertHasAnyElement(members);
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
            // TODO done umeyan "若い順で並べる" をお願いします by jflute (2025/01/07)
            cb.query().addOrderBy_Birthdate_Asc();
            cb.query().addOrderBy_MemberId_Asc();
        });
        // [1on1でのふぉろー]
        // MEMBER_SECURITYの方:
        // 1:1でも
        //  1:必ず存在する1
        //  1:いないかもしれない1
        // というのがある。
        // (個人的にはファーストレベル、セカンドレベルのカーディナリティと呼んでいる)
        //
        // MEMBER_STATUSの方:
        // n:1になるけれども、こっちは物理制約がある
        // NotNull制約、かつ、FK制約
        // 
        // リレーションシップ線はFK制約なので物理制約だけど、黒丸は人間の決め事(業務制約)
        //
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        });
        
        // [1on1でのふぉろー] サロゲートキーの話
        // http://dbflute.seasar.org/ja/manual/topic/dbdesign/surrogatekey.html
        // もし、突っ込んでみたかったら読んでみて。
    }

    // TODO umeyan サブ要件を読んで改めて実装を進めてみてください by jflute (2025/01/07)
    
    public void test_会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberSecurityAsOne().get().getReminderQuestion().contains("2"));
        });
    }

    public void test_会員ステータスの表示順カラムで会員を並べて検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().addOrderBy_MemberStatusCode_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        for (int i = 0; i < members.size() - 1; i++) {
            String currentStatusCode = members.get(i).getMemberStatusCode();
            String nextStatusCode = members.get(i + 1).getMemberStatusCode();
            assertTrue(currentStatusCode.compareTo(nextStatusCode) <= 0);
        }
    }

    public void test_生年月日が存在する会員の購入を検索する() {
        // ## Arrange ##
    	// TODO umeyan ConditionBeanの目的ドリブンの「1個目のステップ」を確認し直してみてください by jflute (2025/01/07)
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/about.html#purpose
    	
        // ## Act ##
        // ## Assert ##
    }

    public void test_2005年10月の1日から3日までに正式会員になった会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setFormalizedDatetime_FromTo(toLocalDateTime("2005-10-01"), toLocalDateTime("2005-10-03"), op -> op.compareAsMonth());
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getFormalizedDatetime().isAfter(toLocalDateTime("2005-09-30")));
            assertTrue(member.getFormalizedDatetime().isBefore(toLocalDateTime("2005-10-04")));
        });
    }
}
