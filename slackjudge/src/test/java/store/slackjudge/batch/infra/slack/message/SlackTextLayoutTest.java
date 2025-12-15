package store.slackjudge.batch.infra.slack.message;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SlackTextLayoutTest {

    private SlackTextLayout layout;

    @BeforeEach
    void setUp() {
        this.layout = new SlackTextLayout();
    }

    @DisplayName("제목은 [ title ] 형태로 반환")
    @Test
    void title() {
        //given
        String text = "slack";

        //when
        String result = layout.title(text);

        //then
        assertThat(result).isEqualTo("[ slack ]");
    }

    @Test
    @DisplayName("라인이 null인 경우 빈 문자열 반환")
    void section_null() {
        //given
        List<String> lines = null;
        String name = "name";

        //when
        String result = layout.section(name, lines);

        //then
        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("라인이 empty인 경우 빈 문자열 반환")
    void section_empty() {
        //given
        List<String> lines = new ArrayList<>();
        String name = "name";

        //when
        String result = layout.section(name, lines);

        //then
        assertThat(result).isEqualTo("");
    }

    @Test
    @DisplayName("라인 여러 줄을 섹션 포멧으로 반환")
    void section_success() {
        //given
        List<String> lines = List.of(
                "Job name : slackJudge",
                "Batch Time : 2025-12-03"
        );

        //when
        String result = layout.section("배치 시작", lines);

        //then
        assertThat(result).isEqualTo(
                "\n✏️ 배치 시작\n" +
                "- Job name : slackJudge\n" +
                "- Batch Time : 2025-12-03\n"
        );
    }

    @Test
    void kv() {
        //when
        String result = layout.kv("key", "value");

        //then
        assertThat(result).isEqualTo("key : value");
    }

    @Test
    @DisplayName("text가 null이면 빈 코드블록 반환")
    void codeBlock_null() {
        //given
        String text = null;

        //when
        String result = layout.codeBlock(text);
        //then
        assertThat(result).isEqualTo("``````");
    }

    @Test
    @DisplayName("text가 공백이면 빈 코드블록 반환")
    void codeBlock_blank() {
        //given
        String text = "   ";

        //when
        String result = layout.codeBlock(text);
        //then
        assertThat(result).isEqualTo("``````");
    }

    @Test
    void codeBlock_success() {
        //given
        String text = "Error";

        //when
        String result = layout.codeBlock(text);

        //then
        assertThat(result).isEqualTo("```Error```");
    }

    @Test
    void footer() {
        //given
        String text = "text";

        //when
        String result = layout.footer(text);

        //then
        assertThat(result).isEqualTo("\n- text");
    }

    @Test
    void render() {
        //when
        String result = layout.render(
                "[ TITLE ]",
                "\nCONTENT",
                "\n- FOOTER"
        );

        //then
        assertThat(result).isEqualTo("[ TITLE ]\nCONTENT\n- FOOTER");
    }
}