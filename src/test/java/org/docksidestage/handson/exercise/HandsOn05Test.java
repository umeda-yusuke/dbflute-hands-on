package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.allcommon.CDef;
import org.docksidestage.handson.dbflute.exbhv.MemberAddressBhv;
import org.docksidestage.handson.dbflute.exbhv.PurchaseBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.dbflute.exentity.MemberAddress;
import org.docksidestage.handson.dbflute.exentity.Purchase;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn05Test extends UnitContainerTestCase {
    @Resource
    private MemberAddressBhv memberAddressBhv;

    @Resource
    private PurchaseBhv purchaseBhv;

    /**
     * 会員名称、有効開始日、有効終了日、住所、地域名称をログに出して確認する
     * 会員IDの昇順、有効開始日の降順で並べる
     */
    public void test_会員住所情報を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<MemberAddress> addresses = memberAddressBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.setupSelect_Region();
            cb.query().addOrderBy_MemberId_Asc();
            cb.query().addOrderBy_ValidBeginDate_Desc();
        });
        // ## Assert ##
        addresses.forEach(address -> {
            Member member = address.getMember().get();
            log("会員名称: " + member.getMemberName(), "有効開始日: " + address.getValidBeginDate(),
                    "有効終了日: " + address.getValidEndDate(), "住所: " + address.getAddress(),
                    "地域名称: " + address.getRegion().get().getRegionName());
        });
    }

    /**
     * SetupSelectのJavaDocにcommentがあることを確認すること
     * 会員名称と住所をログに出して確認すること
     * 現在日付はスーパークラスのメソッドを利用 ("c" 始まりのメソッド)
     * 会員住所情報が取得できていることをアサート
     */
    public void test_会員と共に現在の住所を取得して検索する() {
        // ## Arrange ##
        // ## Act ##
        // [1on1でのふぉろー] 業務的one-to-oneを使ったやり方
        //ListResultBean<Member> memberList = memberBhv.selectList(cb -> {
        //    cb.setupSelect_MemberAddressAsValid(currentLocalDate());
        //});
        List<MemberAddress> addresses = memberAddressBhv.selectList(cb -> {
            cb.setupSelect_Member();
            cb.query().setValidBeginDate_LessEqual(currentLocalDate());
            cb.query().setValidEndDate_GreaterEqual(currentLocalDate());
        });
        // ## Assert ##
        assertHasAnyElement(addresses);
        addresses.forEach(address -> {
            Member member = address.getMember().get();
            log("会員名称: " + member.getMemberName(), "住所: " + address.getAddress(), "開始日: " + address.getValidBeginDate(),
                    "終了日: " + address.getValidEndDate());
        });
    }

    // フォーマッターを掛けるタイミング:
    //  A. IDE上で掛ける // for UX
    //  B. コミット時に自動で掛かる (チェックが掛かるだけの場合も) // for data
    //  C. コミット後に自動で掛かる (チェックが掛かるだけの場合も) // for data
    //
    // ※バリデーションの話も交えて、「UXのため」と「dataのため」
    // ※spotlessのちょい紹介
    // ※他の現場で保存ボタンで自動フォーマットするやり方の紹介
    /***
     * 会員ステータス名称と住所をログに出して確認すること
     * 購入に紐づいている会員の住所の地域が千葉であることをアサート
     */
    public void test_千葉に住んでいる会員の支払済み購入を検索する() {
        // ## Arrange ##
        // ## Act ##
        List<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            
            cb.setupSelect_Member().withMemberAddressAsValid(currentLocalDate()).withRegion();
            cb.query().setPaymentCompleteFlg_Equal_True();
            // TODO umeyan queryRegion()まで行かなくてOK (MEMBER_ADDRESSがREGION_IDを持っているため) by jflute (2025/05/08)
            cb.query().queryMember().queryMemberAddressAsValid(currentLocalDate()).queryRegion().setRegionId_Equal_千葉();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            Member member = purchase.getMember().get();
            MemberAddress address = member.getMemberAddressAsValid().get();
            // [tips] フォーマッターの少し空気読んで欲しいを、強引にやる方法 → // を後ろ付ける
            log("会員名称: " + member.getMemberName(),
                    "会員ステータス: " + member.getMemberStatusCode(),
                    "住所: " + address.getAddress(),
                    "地域名称: " + address.getRegion().get().getRegionName(),
                    "購入日時: " + purchase.getPurchaseDatetime()
            );
            // TODO umeyan getRegion()まで行かなくてOK (MEMBER_ADDRESSがREGION_IDを持っているため) by jflute (2025/05/08)
            assertTrue(address.getRegion().get().isRegionId千葉());
            assertTrue(purchase.isPaymentCompleteFlgTrue());
        });
    }
}
