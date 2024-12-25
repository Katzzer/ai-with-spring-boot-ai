package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final SimpleVectorStore vectorStore;

    public OpenAIServiceImpl(ChatModel chatModel, SimpleVectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @Value("classpath:templates/get-capital-prompt.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/get-capital-with-info.st")
    private Resource getCapitalPromptWithInfo;

    @Value("classpath:templates/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @Value("classpath:templates/uhk-rag-prompt-template.st")
    private Resource uhkPromptTemplate;

    @Override
    public String askQuestion(QuestionFromWeb questionFromWeb) {
        QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.fromString(questionFromWeb.getQuestionTypeString());
        Question question = new Question(questionFromWeb.getQuestion());
        GetCapitalRequest getCapitalRequest = new GetCapitalRequest(question.question());

        return switch (questionTypeEnum) {
            case GENERAL_QUESTION -> askGeneralQuestion(question.question());
            case FILM_QUESTION -> getMovieInfo(question).answer();
            case UHK_DOCUMENTATION -> askUhkInfo(question).answer();
            case CAPITAL_CITY_QUESTION -> getCapital(getCapitalRequest).answer(); // TODO: refactor this method
            case CAPITAL_CITY_WITH_MORE_INFO_QUESTION -> getCapitalWithInfo(getCapitalRequest);
        };
    }

    @Override
    public Answer askUhkInfo(Question question) {
        // TODO: merge it with getMovieInfo()
        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .query(question.question()).withTopK(4));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        // only for debugging
        contentList.forEach(System.out::println);

        PromptTemplate promptTemplate = new PromptTemplate(uhkPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of(
                "input", question.question(),
                "documents", String.join("\n", contentList)));

        ChatResponse response = chatModel.call(prompt);

        return new Answer(response.getResult().getOutput().getContent());
    }

    @Override
    public Answer getMovieInfo(Question question) {
       List<Document> documents = vectorStore.similaritySearch(SearchRequest
               .query(question.question()).withTopK(4));
       List<String> contentList = documents.stream().map(Document::getContent).toList();

        // only for debugging
       contentList.forEach(System.out::println);

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of(
                "input", question.question(),
                "documents", String.join("\n", contentList)));

        ChatResponse response = chatModel.call(prompt);

        return new Answer(response.getResult().getOutput().getContent());
    }

    @Override
    public GetCapitalResponse getCapital(GetCapitalRequest getCapitalRequest) {
        BeanOutputConverter<GetCapitalResponse> parser = new BeanOutputConverter<>(GetCapitalResponse.class);
        String format = parser.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "stateOrCountry", getCapitalRequest,
                "format", format)
        );

        ChatResponse response = chatModel.call(prompt);

        return parser.convert(response.getResult().getOutput().getContent());
    }

    @Override
    public String getCapitalWithInfo(GetCapitalRequest getCapitalRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPromptWithInfo);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest));
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    @Override
    public String askGeneralQuestion(String question) { // TODO: change to question
        String limitedQuestion = limitWords(question, 20);
        PromptTemplate promptTemplate = new PromptTemplate("Provide a very short response: " + limitedQuestion);
        Prompt prompt = promptTemplate.create();

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    private String limitWords(String text, int wordLimit) {
        String[] words = text.split("\\s+");
        if (words.length > wordLimit) {
            return String.join(" ", java.util.Arrays.copyOf(words, wordLimit));
        }
        return text;
    }

}
