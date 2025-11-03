package ma.emsi.moughit.tp2webmoughit.llm;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.data.message.SystemMessage;

import jakarta.enterprise.context.Dependent;
import jakarta.annotation.PreDestroy;

import java.io.Serializable;

/**
 * Gère l'interface avec l'API de Gemini.
 * Son rôle est essentiellement de lancer une requête à chaque nouvelle
 * question qu'on veut envoyer à l'API.
 *
 * De portée dependent pour réinitialiser la conversation à chaque fois que
 * l'instance qui l'utilise est renouvelée.
 * Par exemple, si l'instance qui l'utilise est de portée View, la conversation est
 * réunitialisée à chaque fois que l'utilisateur quitte la page en cours.
 */
@Dependent
public class LlmClient implements Serializable {

    private String systemRole;
    private Assistant assistant;
    private ChatMemory chatMemory;

    public LlmClient() {
        // Récupère la clé secrète pour travailler avec l'API du LLM, mise dans une variable d'environnement
        String geminiKey = System.getenv("GEMINI_KEY");
        // Crée une instance de type ChatModel qui représente le LLM.
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .modelName("gemini-2.5-flash") 
                .apiKey(geminiKey)
                .build();

        // Configure la mémoire pour garder jusqu'à 10 messages
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // Crée l'assistant en utilisant la classe AiServices
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * Setter pour le rôle système.
     * Ajoute ce rôle à la mémoire comme SystemMessage.
     * Vide la mémoire avant de définir un nouveau rôle système pour un nouveau contexte.
     * @param systemRole Le rôle système à définir.
     */
    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        if (this.chatMemory != null) {
            this.chatMemory.clear(); // Clear memory when system role changes
            this.chatMemory.add(SystemMessage.from(systemRole));
        }
    }

    /**
     * Envoie une requête au LLM et reçoit une réponse en retour.
     * @param prompt La question à envoyer au LLM.
     * @return La réponse du LLM.
     */
    public String chat(String prompt) {
        if (this.assistant == null) {
            throw new IllegalStateException("LlmClient has not been initialized. Check @PostConstruct method.");
        }
        return this.assistant.chat(prompt);
    }

    @PreDestroy
    public void destroy() {
        // Clean up resources if necessary
        // For LangChain4j models, there's usually no explicit close method needed here.
    }
}
