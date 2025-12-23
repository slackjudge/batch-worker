package store.slackjudge.batch.infra.slack.message;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class SlackTextLayout {
    /**
     * 제목 설정
     */
    public String title(String text){
        return "[ "+text +" ]";
    }

    /**
     * 내용 설정
     */
    public String section(String name, List<String> lines){
        if (lines==null ||lines.isEmpty()) return "";

        return "\n✏️ "+name+"\n"+
               lines.stream().map(l->"- "+l).collect(Collectors.joining("\n"))+"\n";
    }

    /**
     * key-value
     */
    public String kv(String key,String value){
        return key+" : "+value;
    }

    /**
     * 코드 내용
     */
    public String codeBlock(String text){
        if (text==null || text.isBlank()) text="";

        return "```"+text +"```";
    }

    /**
     * footer
     */
    public String footer(String text){
        return "\n- "+text;
    }

    public String render(String... blocks){
        return String.join("",blocks).trim();
    }
}
