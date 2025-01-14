package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalEntity;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// done umeyan javatryと同じく、authorだけでもJavaDocをお願いします by jflute (2024/12/17)
/**
 * The test of hands-on for basic select. (simple cases)
 * @author umeyan
 */
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
        // done umeyan 万が一、テストデータが0件、もしくはActがバグってて検索0件の場合... by jflute (2024/12/17)
        // このままだと、forEach()が素通りして
        assertFalse(members.isEmpty());
        members.forEach(member -> {
            assertTrue(member.getMemberName().startsWith("S"));
        });
    }

    public void test_会員IDが1の会員を取得する() throws Exception {
        // ## Arrange ##
        // ## Act ##
    	// done umeyan [読み物課題] Optionalの変数名の話 by jflute (2024/12/17)
    	// https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalname
    	// 良ければ(こだわりがなければ)、optMemberにしてもらえるとわかりやすいかなと。
        OptionalEntity<Member> optMember = memberBhv.selectEntity(cb -> {
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
        // done umeyan [読み物課題] OptionalEntityの使い方 by jflute (2024/12/17)
        // https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/select/selectentity.html#optionalhandling
        
        // ## Assert ##
        // done umeyan (int)のダウンキャストはなくても動くようにしています by jflute (2024/12/17)
        // assertEquals(int expected, Integer actual) というメソッドがあるので
		assertEquals(1, optMember.get().getMemberId());
		
		// [1on1でのフォロー] 改めてNotNull制約の話をした。
		// ぼくらは「値がない」という状態とどう付き合うか？付き合い方に選択肢があって良し悪しがある。
    }

    // done umeyan [事務連絡] 模範解答的な実装は、最初から見ちゃうとトレーニングが薄くなるけど... by jflute (2024/12/17)
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
        // done umeyan 素通り防止: (今後ずっと同じなので以降のセクションもお願いします) by jflute (2025/01/07)
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertNull(member.getBirthdate());
        });
    }
}

/* [1on1でのふぉろー] ConditionBeanのコンセプト、目的ドリブン

ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
	// (紐づいている)会員ステータスのデータを取得
	cb.setupSelect_MemberStatus(); // select句を設定 => 自然とjoin必要になるので内部で解決される
	//cb.leftOuterJoin(MemberStatus.class).on(自分カラム=相手カラム); // これが要らない

	// (紐づいている)会員ステータスの表示順が2以上の会員に絞り込む
	cb.query().queryMemberStatus().setDisplayOrder_GreaterEqual(2);
	//cb.leftOuterJoin(MemberStatus.class).on(自分カラム=相手カラム); // やっぱりここでも要らない
});

　↓↓↓ なぜ setupSelect だけでいいのか？

select dfloc.MEMBER_ID as MEMBER_ID
     , 会員ステータスのカラムを列挙する (=> Javaに持ってくる)
  from member dfloc
    inner join member_status dfrel_0 on dfloc.MEMBER_STATUS_CODE = dfrel_0.MEMBER_STATUS_CODE
 where dfloc.MEMBER_NAME like 'S%' escape '|'
   and 会員ステータスのカラムで絞り込む
 order by 会員ステータスのカラムで並び替える

　→ なので、joinというのは他の(抽象度が一つ上がった)目的を満たすための手段(と言って過言ではない)
　→ なので、ConditionBeanでは、その目的を指定さえしてもらえれば、joinは自動で構築する
　→ なので、開発者が、目的を指定しつつ、さらにjoinを自分で設定する必要はない


// 前提: そもそもSQLのjoinの挙動
// ただし、"B" のような join だけで絞り込みが発生するようなSQLはあんまり現場では書かない。
// 少なくともDBFluteとしては推奨してない。(ConditionBeanはそういうSQL書かないようにしている)
[A] => 20件
select dfloc.MEMBER_ID as MEMBER_ID
  from member dfloc

[B] => 3件 (joinをするだけで) (内部結合: inner join)
select dfloc.MEMBER_ID as MEMBER_ID
  from member dfloc // 20件
    inner join member_withdrawal on ... // 会員退会情報 (1 : 0..1) // 3人が退会

[C] => 20件 (外部結合: outer join)
select dfloc.MEMBER_ID as MEMBER_ID
  from member dfloc // 20件
    left outer join member_withdrawal on ... // 会員退会情報 (1 : 0..1) // 3人が退会

*/
