package org.docksidestage.handson.exercise;

import java.util.List;

import javax.annotation.Resource;

import org.docksidestage.handson.dbflute.exbhv.MemberBhv;
import org.docksidestage.handson.dbflute.exentity.Member;
import org.docksidestage.handson.unit.UnitContainerTestCase;

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
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setMemberStatusCode_Equal_仮会員();
            cb.setupSelect_MemberStatus();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            log(member);
            assertEquals("仮会員", member.getMemberStatus().get().getMemberStatusName());
        });
    }
}
