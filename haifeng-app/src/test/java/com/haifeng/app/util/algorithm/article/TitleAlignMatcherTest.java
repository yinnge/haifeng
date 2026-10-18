package com.haifeng.app.util.algorithm.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TitleAlignMatcherTest {

    /** 用户需求中的示例标题 */
    private static final String TITLE = "2026年山西省的高考分数线在线查看";

    @Test
    void align_userExample_hitsAllFourSegments() {
        TitleAlignMatcher.AlignResult result = TitleAlignMatcher.align("山西高考2026分数线", TITLE);

        assertThat(result.accepted()).isTrue();
        assertThat(result.getSpans()).hasSize(4);
        // 按 start 升序：2026[0,4) 山西[5,7) 高考[9,11) 分数线[11,14)
        assertThat(result.getSpans().get(0).getStart()).isEqualTo(0);
        assertThat(result.getSpans().get(0).getEnd()).isEqualTo(4);
        assertThat(result.getSpans().get(1).getStart()).isEqualTo(5);
        assertThat(result.getSpans().get(1).getEnd()).isEqualTo(7);
        assertThat(result.getSpans().get(2).getStart()).isEqualTo(9);
        assertThat(result.getSpans().get(2).getEnd()).isEqualTo(11);
        assertThat(result.getSpans().get(3).getStart()).isEqualTo(11);
        assertThat(result.getSpans().get(3).getEnd()).isEqualTo(14);
    }

    @Test
    void align_questionSuffix_isSkippedAsNoise() {
        TitleAlignMatcher.AlignResult result = TitleAlignMatcher.align("山西高考2026分数线是多少", TITLE);

        assertThat(result.accepted()).isTrue();
        assertThat(result.getSpans()).hasSize(4);
    }

    @Test
    void align_punctuationStripped_stillMatches() {
        TitleAlignMatcher.AlignResult result = TitleAlignMatcher.align("山西，高考！2026？分数线。", TITLE);

        assertThat(result.accepted()).isTrue();
    }

    @Test
    void align_unrelatedKeyword_notAccepted() {
        assertThat(TitleAlignMatcher.align("英语四六级真题", TITLE).accepted()).isFalse();
    }

    @Test
    void align_singleCharKeyword_notAccepted() {
        assertThat(TitleAlignMatcher.align("山", TITLE).accepted()).isFalse();
    }

    @Test
    void align_coverageBelowHalf_notAccepted() {
        // 归一化 5 字只命中「山西」2 字（40% < 50%）→ 拒绝
        assertThat(TitleAlignMatcher.align("啊啊啊山西", TITLE).accepted()).isFalse();
    }

    @Test
    void align_blankInputs_notAccepted() {
        assertThat(TitleAlignMatcher.align("  ", TITLE).accepted()).isFalse();
        assertThat(TitleAlignMatcher.align("山西", "").accepted()).isFalse();
        assertThat(TitleAlignMatcher.align(null, TITLE).accepted()).isFalse();
    }
}
