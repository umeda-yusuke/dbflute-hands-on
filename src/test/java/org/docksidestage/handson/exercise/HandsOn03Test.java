package org.docksidestage.handson.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
            // (setBirthdate_IsNotNull()が入れるのであれば、withNullsLast()は意味のないコードになるのでそれなら消す方が良い)
            // (もちろん、要件通り setBirthdate_IsNotNull() が不要なのでそっちを消すでも良い)
            
            // TODO umeyan cbの呼び出し順序の慣習として、select句、where句、order by句... by jflute (2025/01/21)
            // というのがオススメなので、このIsNotNullは、order byよりも前に持っていってくれると嬉しいです。
            // // 実装順序は、データの取得、絞り込み、並び替え | DBFlute
            // http://dbflute.seasar.org/ja/manual/function/ormapper/conditionbean/effective.html#implorder
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
        // (要件通りのカラムでのソートになっていない)
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().addOrderBy_MemberStatusCode_Asc();
            cb.query().addOrderBy_MemberId_Desc();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        for (int i = 0; i < members.size() - 1; i++) {
        	// done umeyan [いいね] 変数名がわかりやすい、current, next by jflute (2025/01/21)
            String currentStatusCode = members.get(i).getMemberStatusCode();
            String nextStatusCode = members.get(i + 1).getMemberStatusCode();
            // TODO umeyan 厳密には、一種類の会員ステータスしか検索されなかった場合に、アサートしたいことが成り立たない by jflute (2025/01/21)
            // "<=" の "<" の部分で成り立った assertTrue() が一回以上あることを保証したい。
            // (一種の素通り防止の話)
            assertTrue(currentStatusCode.compareTo(nextStatusCode) <= 0);
        }
        // [1on1でのふぉろー] UnitTestでのアサートのやり方パターンの話
        // o ロジックをどれだけ書くか？話
        // o テストデータに依存するか？話 (テストデータをどう用意するか？も含めて)
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
            // done umeyan order byの要件を見直し (改めて指差し確認大事) by jflute (2025/01/21)
            cb.query().addOrderBy_PurchaseDatetime_Desc();
            cb.query().addOrderBy_PurchasePrice_Desc();
            cb.query().addOrderBy_ProductId_Asc();
            cb.query().addOrderBy_MemberId_Asc();
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
        	// done umeyan memberは3回も使われてget/getで横長になっているので変数に抽出してください by jflute (2025/01/21)
        	// IntelliJのショートカットでぜひやってみましょう。
            Member member = purchase.getMember().get();
            log(
                    "会員名: " + member.getMemberName(),
                    "会員ステータス: " + member.getMemberStatus().get().getMemberStatusName(),
                    "商品名: " + purchase.getProduct().get().getProductName()
            );
            // done umeyan こっちも抽出した member がそのまま使えるかなと by jflute (2025/01/28)
            assertNotNull(member.getBirthdate());
        });
    }

    /**
     * [6] 2005年10月の1日から3日までに正式会員になった会員を検索

        画面からの検索条件で2005年10月1日と2005年10月3日がリクエストされたと想定して...
        Arrange で String の "2005/10/01", "2005/10/03" を一度宣言してから日時クラスに変換し...
        自分で日付移動などはせず、DBFluteの機能を使って、そのままの日付(日時)を使って条件を設定
        会員ステータスも一緒に取得
        ただし、会員ステータス名称だけ取得できればいい (説明や表示順カラムは不要)
        会員名称に "vi" を含む会員を検索
        会員名称と正式会員日時と会員ステータス名称をログに出力
        会員ステータスがコードと名称だけが取得されていることをアサート
        会員の正式会員日時が指定された条件の範囲内であることをアサート

    ※Java8 (DBFlute-1.1) なら、assertException(...)を使うとよいでしょう 
    ※修行++: 実装できたら、こんどはスーパークラスのメソッド adjustMember_FormalizedDatetime_...() を使って、
    10月1日ジャスト(時分秒なし)の正式会員日時を持つ会員データを作成してテスト実行してみましょう。
    もともと一件しかなかった検索結果が「二件」になるはずです。 
     */
    public void test_2005年10月の1日から3日までに正式会員になった会員を検索する() {
        // ## Arrange ##
        String fromDateText = "2005-10-01";
        String toDateText = "2005-10-03";
        
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            // TODO umeyan 要件 "会員ステータスも一緒に取得" (2025/01/28)
            // TODO umeyan 要件 "会員ステータス名称だけ取得できればいい" (2025/01/28)
            // TODO umeyan SQL見ると、10月まるまるが対象になってしまっています by jflute (2025/01/28)
            //  where dfloc.FORMALIZED_DATETIME >= '2005-10-01 00:00:00.000'
            //    and dfloc.FORMALIZED_DATETIME < '2005-11-01 00:00:00.000'
            // 質問: どこからまでどこまで？ => 10/1 00:00:00 〜 10/4 00:00:00未満
            // ふぉろー: DateFromToのコンセプト
            cb.query().setFormalizedDatetime_FromTo(toLocalDateTime(fromDateText), toLocalDateTime(toDateText), op -> op.compareAsDate());
            // TODO umeyan 要件 "会員名称に "vi" を含む会員を検索" (2025/01/28)
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            // TODO umeyan 要件 "会員名称と正式会員日時と会員ステータス名称をログに出力" by jflute (2025/01/28)
            // TODO umeyan 要件 "会員ステータスがコードと名称だけが取得されていることをアサート" by jflute (2025/01/28)
            // TODO umeyan 型がLocalDateTimeなので「9/30より後(after)」だと、9/30の1ミリ秒以降になっちゃう by jflute (2025/01/28)
            // TODO umeyan 変数の抽出、今後も意識お願いします。なのでここも by jflute (2025/01/28)
            assertTrue(member.getFormalizedDatetime().isAfter(toLocalDateTime("2005-09-30")));
            assertTrue(member.getFormalizedDatetime().isBefore(toLocalDateTime("2005-10-04")));
            
            // LocalDate: 日付 (タイムゾーンを意識しない)
            // LocalDateTime: 日時 (タイムゾーンを意識しない)
            // LocalTime: 時間 HH:mm:ss.SSS (タイムゾーンを意識しない)
            //
            // ZonedDateTime, Offset...: タイムゾーンを意識した日付/日時オブジェクトたち
        });
    }

    /**
     * 会員と会員ステータス、会員セキュリティ情報も一緒に取得
     * 商品と商品ステータス、商品カテゴリ、さらに上位の商品カテゴリも一緒に取得
     * 上位の商品カテゴリ名が取得できていることをアサート
     * 購入日時が正式会員になってから一週間以内であることをアサート
     */
    public void test_正式会員になってから一週間以内の購入を検索する() {
        // ## Arrange ##
        // ## Act ##
    	// [1on1でのふぉろー] ConditionBeanの機能を探すときは？
    	// https://dbflute.seasar.org/ja/tutorial/devfaq.html#howtosearch
    	//
    	// [1on1でのふぉろー] ドキュメントの読み方、ザラ読みは極力やめたいところだが...
    	// せめてザラ読みしたことを覚えておいて、後で持ってくればなんとかなる。
    	// わかってることとわかってないことの整理。
    	//
    	// 「ふんわりわかってる」ということを認識して記憶を保持している事自体はとても良い。
    	// それも粒度の高いレベルでの「わかってるわかってない」を整理していると言える。
    	//
    	// 自分の中でデマを広げさせない | jfluteの日記
    	// https://jflute.hatenadiary.jp/entry/20110619/nodema
    	// 「仮説を仮説のまま頭の中で保持し続ける力」
    	//
        List<Purchase> purchases = purchaseBhv.selectList(cb -> {
            cb.setupSelect_Member().withMemberStatus();
            cb.setupSelect_Member().withMemberSecurityAsOne();
            cb.setupSelect_Product().withProductStatus();
            cb.setupSelect_Product().withProductCategory();
            
            // done umeyan 改行しないLambdaであれば、expressionスタイルでOKです (lessThanは大丈夫) by jflute (2025/01/28)
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .greaterEqual(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime());
            cb.columnQuery(colCB -> colCB.specify().columnPurchaseDatetime())
                    .lessThan(colCB -> colCB.specify().specifyMember().columnFormalizedDatetime())
                    .convert(op -> op.addDay(8));
            
            // [1on1でのふぉろー] 一週間以内の解釈
            //
            // 10/3                    10/10     10/11
            //  13h                      0h  13h   0h
            //   |                       |    |    |
            //   |       D               | I  |    | P
            // A |                       |H  J|L   |O
            //   |C                  E   G    K    N
            //   B                      F|    |   M|
            //   |                       |         |
            //
        });
        // ## Assert ##
        assertHasAnyElement(purchases);
        purchases.forEach(purchase -> {
            // done umeyan 変数の抽出、今後も意識お願いします。なのでここも by jflute (2025/01/28)
            LocalDateTime purchaseDatetime = purchase.getPurchaseDatetime();
            LocalDateTime formalizedDatetime = purchase.getMember().get().getFormalizedDatetime();
            assertTrue(purchaseDatetime.isAfter(formalizedDatetime));
            assertTrue(purchaseDatetime.isBefore(formalizedDatetime.plusDays(8)));
        });
    }

    /**
     * 画面からの検索条件で1974年がリクエストされたと想定
     * Arrange で String の "1974/01/01" を一度宣言してから日付クラスに変換
     * その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現
     * 会員ステータス名称、リマインダ質問と回答、退会理由入力テキストを取得する(ログ出力) ※1
     * 若い順だが生年月日が null のデータを最初に並べる
     * 生年月日が指定された条件に合致することをアサート (1975年1月1日なら落ちるように)
     * Arrangeで "きわどいデータ" ※2 を作ってみましょう (Behavior の updateNonstrict() ※3 を使って)
     * 検索で含まれるはずの "きわどいデータ" が検索されてることをアサート (アサート自体の保証のため)
     * 生まれが不明の会員が先頭になっていることをアサート
     */
    public void test_1974年までに生まれたもしくは不明の会員を検索する() {
        // ## Arrange ##
        LocalDate targetDate = toLocalDate("1974-01-01");
        Member targetMember = new Member();
        targetMember.setMemberId(99999);
        targetMember.setMemberName("きわどいデータ");
        targetMember.setMemberAccount("kiwadoi");
        targetMember.setBirthdate(targetDate);
        targetMember.setMemberStatusCode("FML");
        targetMember.setRegisterDatetime(currentLocalDateTime());
        targetMember.setRegisterUser("test");
        targetMember.setUpdateDatetime(LocalDateTime.now());
        targetMember.setUpdateUser("test");
        memberBhv.insert(targetMember);

        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            // TODO umeyan ここも FromTo を使って欲しいところ by jflute (2025/01/28)
            cb.query().setBirthdate_LessEqual(toLocalDate("1974-12-31"));
            cb.query().addOrderBy_Birthdate_Desc().withNullsFirst();
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            log(member.getBirthdate());
            assertTrue(member.getBirthdate().isBefore(toLocalDate("1975-01-01")));
        });
    }

    /**
     * 画面からの検索条件で2005年6月がリクエストされたと想定
     * Arrange で String の "2005/06/01" を一度宣言してから日付クラスに変換
     * その日付クラスの値を、(日付移動などせず)そのまま使って検索条件を実現
     * 第二ソートキーは会員IDの降順
     * 検索された会員の生年月日が存在しないことをアサート
     * 2005年6月に正式会員になった会員が先に並んでいることをアサート (先頭だけじゃなく全体をチェック)
     */
    public void test_2005年6月に正式会員になった会員を先に並べて生年月日のない会員を検索する() {
        // ## Arrange ##
        LocalDateTime targetDate = toLocalDateTime("2005-06-01");
        // ## Act ##
        List<Member> members = memberBhv.selectList(cb -> {
            cb.query().setFormalizedDatetime_FromTo(targetDate, targetDate.plusMonths(1), op -> op.compareAsMonth());
            cb.query().setBirthdate_IsNull();
            cb.query().addOrderBy_MemberId_Desc();
            // [1on1でのふぉろー] 2005年6月は、絞り込むわけじゃなく、並び替えで優先的に前に並べるという要件
            // 「ConditionBeanの機能の探し方」でぜひ探してみてください。
        });
        // ## Assert ##
        assertHasAnyElement(members);
        members.forEach(member -> {
            assertNull(member.getBirthdate());
            assertTrue(member.getFormalizedDatetime().isAfter(targetDate));
            assertTrue(member.getFormalizedDatetime().isBefore(targetDate.plusMonths(1)));
        });
    }

    /**
     * 会員ステータス名称も取得
     * 会員IDの昇順で並べる
     * ページサイズは 3、ページ番号は 1 で検索すること
     * 会員ID、会員名称、会員ステータス名称をログに出力
     * SQLのログでカウント検索時と実データ検索時の違いを確認
     * 総レコード件数が会員テーブルの全件であることをアサート
     * 総ページ数が期待通りのページ数(計算で導出)であることをアサート
     * 検索結果のページサイズ、ページ番号が指定されたものであることをアサート
     * 検索結果が指定されたページサイズ分のデータだけであることをアサート
     * PageRangeを 3 にして PageNumberList を取得し、[1, 2, 3, 4]であることをアサート
     * 前のページが存在しないことをアサート
     * 次のページが存在することをアサート
     */
    public void test_全ての会員をページング検索する() {
        // ## Arrange ##
        // ## Act ##
        ListResultBean<Member> memberList = memberBhv.selectPage(cb -> {
            cb.setupSelect_MemberStatus();
            cb.query().addOrderBy_MemberId_Asc();
            cb.paging(3, 1);
        });
        // ## Assert ##
        assertHasAnyElement(memberList);
        memberList.forEach(member -> {
            log("会員ID: " + member.getMemberId(), "会員名: " + member.getMemberName(), "会員ステータス: " + member.getMemberStatus().get().getMemberStatusName());
        });
    }
}
