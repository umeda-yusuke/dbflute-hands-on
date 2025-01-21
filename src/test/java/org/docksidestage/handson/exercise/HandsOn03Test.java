package org.docksidestage.handson.exercise;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.handson.dbflute.cbean.MemberCB;
import org.docksidestage.handson.dbflute.cbean.PurchaseCB;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.MemberSecurityBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberSecurity;
import org.docksidestage.handson.dbflute.exentity.Purchase;
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
    @Resource
    private PurchaseBhv purchaseBhv;

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
            // done umeyan "若い順で並べる" をお願いします by jflute (2025/01/07)
            //
            // [1on1でのふぉろー]
            // DBMSによって、nullのデータが先に並ぶのか？後に並ぶのか？変わってくる。
            // なので、基本的には null のデータを曖昧にする order by にしないほうが無難。
            //
            // Oracle, PostgreSQL とかだと nulls first/last っていうSQL文法があります。
            // MySQLはないので、case when で代用する。DBMSによってSQLの文法がちょっと違う。
            //
            // done umeyan 若い順になってない by jflute (2025/01/14)
            cb.query().addOrderBy_Birthdate_Desc().withNullsLast();
            cb.query().addOrderBy_MemberId_Asc();
            // TODO umeyan 要件を変えたとしても、birthdateがnotnullのものだけに絞るなら NullsLast が不要になる by jflute (2025/01/21)
            // TODO umeyan cbの呼び出し順序の慣習として、select句、where句、order by句... by jflute (2025/01/21)
            // というのがオススメなので、このIsNotNullは、order byよりも前に持っていってくれると嬉しいです。
            cb.query().setBirthdate_IsNotNull();
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
        	log(member.getMemberName(), member.getBirthdate());
            assertTrue(member.getMemberStatus().isPresent());
            assertTrue(member.getMemberSecurityAsOne().isPresent());
        });
        
        // [1on1でのふぉろー] サロゲートキーの話
        // http://dbflute.seasar.org/ja/manual/topic/dbdesign/surrogatekey.html
        // もし、突っ込んでみたかったら読んでみて。
    }

    @Resource
	private MemberSecurityBhv memberSecurityBhv;
    // done umeyan サブ要件を読んで改めて実装を進めてみてください by jflute (2025/01/07)
    // javadocにハンズオンのページの検索要件をコピーして持ってくると楽です。
    /**
     * 会員セキュリティ情報のデータ自体は要らない
     * (Actでの検索は本番でも実行されることを想定し、テスト都合でパフォーマンス劣化させないこと)
     * リマインダ質問に2が含まれていることをアサート
     * アサートするために別途検索処理を入れても誰も文句は言わない
     */
    public void test_会員セキュリティ情報のリマインダ質問で2という文字が含まれている会員を検索する() {
        // ## Arrange ##
        // ## Act ##
    	// [1on1での質問] "会員セキュリティ情報のデータ自体は要らない" の話
    	// [1on1ふぉろー] セクション2のConditionBeanのコンセプト、目的ドリブンの話を再度じっくりした。
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        // ## Assert ##
        // [1on1ふぉろー] テスト対象のプログラムが戻すデータですべてアサートが満たせるとは限らない。
        // ので、Assertの中でアサートに必要なデータを自分でかき集めてきても良い。
        List<Member> reminderQuestion2Members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberSecurityAsOne();
            cb.query().queryMemberSecurityAsOne().setReminderQuestion_LikeSearch("2", op -> op.likeContain());
        });
        assertHasAnyElement(members);
        assertEquals(reminderQuestion2Members.size(), members.size());
        // done umeyan [思考エクササイズ] 超厳密には、order byを入れていないselectの結果の順序は保証されない by jflute (2025/01/21)
        // このやり方で本気でそこも配慮するなら、順序は気にせず組み合わせとして会員のセットが同じであることをアサートするといいのかもしれない。
        for (int i = 0; i < members.size(); i++) {
            assertEquals(reminderQuestion2Members.get(i).getMemberName(), members.get(i).getMemberName());
        }
        
        // [1on1でのふぉろー] jfluteライブコーディング
        //ListResultBean<MemberSecurity> securityList = memberSecurityBhv.selectList(cb -> {
        //	Collection<Integer> memberIdList = members.stream().map(mb -> {
        //		return mb.getMemberId();
        //	}).collect(Collectors.toList());
        //	cb.query().setMemberId_InScope(memberIdList);
        //});
        //assertHasAnyElement(securityList);
        //assertEquals(members.size(), securityList.size());
        //for (Member member : members) {
        //	boolean hasSecurity = false;
        //	for (MemberSecurity security : securityList) {
        //		if (member.getMemberId().equals(security.getMemberId())) {
        //			assertTrue(security.getReminderQuestion().contains("2"));
        //			hasSecurity = true;
        //		}
        //	}
        //	assertTrue(hasSecurity);
        //}
        
        // [1on1でのふぉろー]
        // 模範解答的な実装を一緒に見て、いろいろなやり方を学んだ。
    }

    public void test_会員ステータスの表示順カラムで会員を並べて検索する() {
        // ## Arrange ##
        // ## Act ##
    	// TODO umeyan [最後に修正でOK] "会員ステータスの表示順カラム" なので、会員ステータスコードで並べるわけではない by jflute (2025/01/21)
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().addOrderBy_MemberStatusCode_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        for (int i = 0; i < members.size() - 1; i++) {
        	// TODO umeyan [いいね] 変数名がわかりやすい、current, next by jflute (2025/01/21)
            String currentStatusCode = members.get(i).getMemberStatusCode();
            String nextStatusCode = members.get(i + 1).getMemberStatusCode();
            // TODO umeyan 厳密には、一種類の会員ステータスしか検索されなかった場合に、アサートしたいことが成り立たない by jflute (2025/01/21)
            // "<=" の "<" の部分で成り立った assertTrue() が一回以上あることを保証したい。
            assertTrue(currentStatusCode.compareTo(nextStatusCode) <= 0);
        }
    }

    /**
     * 会員名称と会員ステータス名称と商品名を取得する(ログ出力)
     * 購入日時の降順、購入価格の降順、商品IDの昇順、会員IDの昇順で並べる
     * OrderBy がたくさん追加されていることをログで目視確認すること
     * 購入に紐づく会員の生年月日が存在することをアサート
     */
    public void test_生年月日が存在する会員の購入を検索する() {
        // ## Arrange ##
    	// done umeyan ConditionBeanの目的ドリブンの「1個目のステップ」を確認し直してみてください by jflute (2025/01/07)
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/about.html#purpose
        // ## Act ##
        List<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.query().queryMember().setBirthdate_IsNotNull();
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Product();
            // TODO umeyan order byの要件を見直し (改めて指差し確認大事) by jflute (2025/01/21)
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().queryProduct().addOrderBy_ProductId_Asc();
            cb.query().queryProduct().addOrderBy_ProductId_Asc();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
        	// TODO umeyan memberは3回も使われてget/getで横長になっているので変数に抽出してください by jflute (2025/01/21)
        	// IntelliJのショートカットでぜひやってみましょう。
            log(
                    "会員名: " + purchase.getMember().get().getMemberName(),
                    "会員ステータス: " + purchase.getMember().get().getMemberStatus().get().getMemberStatusName(),
                    "商品名: " + purchase.getProduct().get().getProductName()
            );
            assertNotNull(purchase.getMember().get().getBirthdate());
        });
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
