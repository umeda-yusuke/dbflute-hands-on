import java.util.List;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// TODO umeyan javatryと同じく、authorだけでもJavaDocをお願いします by jflute (2024/12/17)
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
        // TODO umeyan 万が一、テストデータが0件、もしくはActがバグってて検索0件の場合... by jflute (2024/12/17)
        // このままだと、forEach()が素通りして
        members.forEach(member -> {
            assertTrue(member.getMemberName().startsWith("S"));
        });
    }

    public void test_会員IDが1の会員を取得する() throws Exception {
        // ## Arrange ##
        // ## Act ##
    	// TODO umeyan [読み物課題] Optionalの変数名の話 by jflute (2024/12/17)
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalname
    	// 良ければ(こだわりがなければ)、optMemberにしてもらえるとわかりやすいかなと。
        OptionalEntity<Member> member = memberBhv.selectEntity(cb -> {
            cb.query().setMemberId_Equal(1);
        });

        // [1on1でのフォロー] DBFluteのOptionalの紹介、alwaysPresent()が使える
		//  member.ifPresent(mb -> {
		//	    assertEquals(1, mb.getMemberId());
		//  }); // 万が一のときに素通りする可能性あり
        //
        //  // メールを飛ばす処理
        //  // クレジットカードの決済 (万が一でも変な状態で動いて欲しくない)
        //  // ...
        //  ↓↓↓
		// member.alwaysPresent(mb -> { // なかったら例外で落ちる (メッセージがリッチ)
		//	  assertEquals(1, mb.getMemberId());
		// });
        // 実務だと中途半端に動いて欲しくないので、なかったら例外ですぐ落としたい。
        // でも、例外メッセージはデバッグしやすいものになっていて欲しい。
        // TODO umeyan [読み物課題] OptionalEntityの使い方 by jflute (2024/12/17)
        // https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalhandling
        
        // ## Assert ##
        // TODO umeyan (int)のダウンキャストはなくても動くようにしています by jflute (2024/12/17)
        // assertEquals(int expected, Integer actual) というメソッドがあるので
		assertEquals(1, (int) member.get().getMemberId());
		
		// [1on1でのフォロー] 改めてNotNull制約の話をした。
		// ぼくらは「値がない」という状態とどう付き合うか？付き合い方に選択肢があって良し悪しがある。
    }

    // TODO umeyan [事務連絡] 模範解答的な実装は、最初から見ちゃうとトレーニングが薄くなるけど... by jflute (2024/12/17)
    // 少し自力で頑張ってぐぬぬとなったらチラ見するとか...
    // 実装できた後に模範解答的な実装と見比べて「あー、なるほど」とかの学びにするとか...
    // そういう感じで使ってもらえればと思います。
    // (まあ根性出して全く見ないってのもアリですが(^^、結局ぼくのレビューは模範を基軸に入れます)
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
