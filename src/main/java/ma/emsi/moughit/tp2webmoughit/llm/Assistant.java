package ma.emsi.moughit.tp2webmoughit.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    String chat(@UserMessage String prompt);
}
