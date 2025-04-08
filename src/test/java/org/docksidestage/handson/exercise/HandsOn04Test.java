package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Resource;

import org.dbflute.optional.OptionalScalar;
import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.Purchase;
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

//
// [1on1でのふぉろー]
// 振り返りのお話
//
// 良かったことを続けるために、良かったことを見失わないこと
// https://jflute.hatenadiary.jp/entry/20170826/keepgoodtime
//
// 振り返りを終えて、Tryはどこへ？
// https://jflute.hatenadiary.jp/entry/20140103/1388728678
//
// 振り返りで得た、良いだろうと思われる手順を、無意識にできるようにするためには？話
//

public class HandsOn04Test extends UnitContainerTestCase {
    @Resource
    private MemberBhv memberBhv;
    @Resource
    private PurchaseBhv purchaseBhv;

    /**
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 支払完了フラグは "0" で未払い。ひとまずベタで
     * 購入日時の降順で並べる
     * 会員名称と商品名と一緒にログに出力
     * 購入が未払いであることをアサート
     */
    public void test_退会会員の未払い購入を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Product();
            cb.query().queryMember().setMemberStatusCode_Equal_退会会員();
            cb.query().setPaymentCompleteFlg_Equal_False();
            cb.query().addOrderBy_PurchaseDatetime_Desc();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            log("会員名称: " + purchase.getMember().get().getMemberName(),
                    "商品名: " + purchase.getProduct().get().getProductName(),
                    "購入日時: " + purchase.getPurchaseDatetime(),
                    "会員ステータス: " + purchase.getMember().get().getMemberStatusCode()
            );
            assertTrue(purchase.isPaymentCompleteFlgFalse());
        });
    }

    /**
     * 退会会員でない会員は、会員退会情報を持っていないことをアサート
     * 退会会員のステータスコードは "WDL"。ひとまずベタで
     * 不意のバグや不意のデータ不備でもテストが(できるだけ)成り立つこと
     */
    public void test_会員退会情報も取得して会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.setupSelect_MemberWithdrawalAsOne();
            // TODO umeyan "会員退会情報も取得して会員を検索する" なので絞り込みはせずに検索 by jflute (2025/04/08)
            cb.query().setMemberStatusCode_NotEqual_退会会員();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertTrue(member.getMemberWithdrawalAsOne().isEmpty());
        });
    }

    /**
     * 区分値メソッドの JavaDoc コメントを確認する
     * 会員ステータス名称も取得する(ログに出力)
     * 会員が仮会員であることをアサート
     */
    public void test_一番若い仮会員の会員を検索する() {
        // ## Arrange ##
        // ## Act ##
        // done umeyan 修行++: 同率首位を検索できるようにしてみましょう by jflute (2025/03/25)
        // [1on1でのふぉろー] ScalarConditionで一発取りもできる
        //memberBhv.selectList(cb -> {
        //    cb.query().scalar_Equal().max(memberCB -> {
        //        memberCB.specify().columnBirthdate();
        //        memberCB.query().setMemberStatusCode_Equal_仮会員();
        //    });
        //    cb.query().setMemberStatusCode_Equal_仮会員();
        //});
        
        OptionalScalar<LocalDate> latestBirthdate = memberBhv.selectScalar(LocalDate.class).max(cb -> {
            cb.specify().columnBirthdate();
            cb.query().setMemberStatusCode_Equal_仮会員();
        });
        // (その場合、selectList() になる)
        // (ConditionBean (cb) の機能で、そういう検索ができるものがある)
        // (わからなかったら、まずでSQLで考えてみてみましょう。SQLで同率首位をどう取るか？)

        // done umeyan "一番若い" の条件が抜けている by jflute (2025/03/18)
        List<Member> members = memberBhv.selectList(cb -> {
            // TODO done umeyan select句にまつわるものを先に書いて、where句のものを後に by jflute (2025/03/25)
            cb.setupSelect_MemberStatus();
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.query().setBirthdate_Equal(latestBirthdate.get());
            // done umeyan 一番年齢の高い人が取れちゃってる (日付は少ない方が年齢が高いもの) by jflute (2025/03/25)
            // TODO umeyan ここまで来ると、そもそも BIRTHDATE によるソートは意味がなくなっている by jflute (2025/04/08)
            cb.query().addOrderBy_Birthdate_Desc().withNullsLast();
            // fetchFirst(1)自体は良くて、1と決め売ったからにはリストにはならず絶対に一件なので...
            // そもそも selectList() じゃなくて selectEntity() の検索で良い。
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            // [1on1でのふぉろー] こういう確認のためのログをアサートの前に入れちゃってもOK
            log(member.getBirthdate());
            // [1on1でのふぉろー] is判定メソッドも使えます
            assertTrue(member.isMemberStatusCode仮会員());
        });
    }
    
    // [1on1でのふぉろー] select系メソッド
    // o カウント検索
    // o 一件検索
    // o リスト検索
    // o ページング検索
    // o カーソル検索
    // o (スカラ検索)
    // https://dbflute.seasar.org/ja/manual/function/ormapper/behavior/index.html

    // [1on1でのふぉろー] 普通サブクエリと相関サブクエリ
    // o それぞれの特徴
    // o MySQLの最新だと？
    // o 他のDBだと？(軽くSQLを書き換えて最適化する話)
    // o 有料DBの強さ
    // o ITの大企業の歴史的なお話
}
