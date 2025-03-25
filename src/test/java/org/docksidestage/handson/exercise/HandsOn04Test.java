package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

// [1on1でのふぉろー]
// // エラーメッセージ読め読め大合唱
// https://jflute.hatenadiary.jp/entry/20130522/errorsinging
// → ログは解読するもの、プロとして、という意識
//
// // 問題分析と問題解決を分けることがハマらない第一歩
// https://jflute.hatenadiary.jp/entry/20170712/analysissolving
// → 分析しなければ仮説は当てずっぽう
//
// // 逆にテーブルスキャンもできなきゃね
// https://jflute.hatenadiary.jp/entry/20150112/tablescan
// → 仮説の脳みそと検証の脳みそは違うもの
//
// 振り返りの判断基準の候補: 判断、その中の「手順」
// 手順は結果が成功でも反省しないといけない。

public class HandsOn04Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;

    /**
     * 区分値メソッドの JavaDoc コメントを確認する
     * 会員ステータス名称も取得する(ログに出力)
     * 会員が仮会員であることをアサート
     */
    public void test_一番若い仮会員の会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        // TODO done umeyan "一番若い" の条件が抜けている by jflute (2025/03/18)
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_Birthdate_Asc().withNullsLast();
            cb.fetchFirst(1);
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            // [1on1でのふぉろー] is判定メソッドも使えます
            assertTrue(member.isMemberStatusCode仮会員());
        });
    }
}
