package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.exceptionHandling.BadRequestException;
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
import java.util.Objects;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final SimpleVectorStore vectorStore;

    public OpenAIServiceImpl(ChatModel chatModel, SimpleVectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @Value("classpath:templates/prompts/get-capital-prompt.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/prompts/get-capital-prompt-with-more-info.st")
    private Resource getCapitalPromptWithMoreInfo;

    @Value("classpath:templates/prompts/rag-prompt-template.st")
    private Resource movieRagPromptTemplate;

    @Value("classpath:templates/prompts/uhk-rag-prompt-template.st")
    private Resource uhkPromptTemplate;

    @Value("classpath:templates/prompts/general-question-prompt-template.st")
    private Resource generalQuestionPromptTemplate;

    @Override
    public String askQuestion(QuestionFromWeb questionFromWeb) {

        QuestionTypeEnum questionTypeEnum = QuestionTypeEnum.fromString(questionFromWeb.getQuestionTypeString());
        String question = questionFromWeb.getQuestion();
        checkPrompt(question, questionTypeEnum);

        return switch (questionTypeEnum) {
            case GENERAL_QUESTION -> askGeneralQuestion(question);
            case FILM_QUESTION, UHK_DOCUMENTATION -> getDataFromVectorStore(question, questionTypeEnum);
            case CAPITAL_CITY_QUESTION -> getCapitalOfCountry(question);
            case CAPITAL_CITY_WITH_MORE_INFO_QUESTION -> getCapitalOfCountryWithMoreInfo(question);
        };
    }

    private String getDataFromVectorStore(String question, QuestionTypeEnum questionTypeEnum) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .query(question).withTopK(4));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        // only for debugging
        contentList.forEach(System.out::println);

        Resource resource = movieRagPromptTemplate;
        if (questionTypeEnum == QuestionTypeEnum.UHK_DOCUMENTATION) {
            resource = uhkPromptTemplate;
        }

        PromptTemplate promptTemplate = new PromptTemplate(resource);
        Prompt prompt = promptTemplate.create(Map.of(
                "input", question,
                "documents", String.join("\n", contentList)));

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    private String getCapitalOfCountry(String question) {

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "stateOrCountry", question));

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    private String getCapitalOfCountryWithMoreInfo(String question) {
        BeanOutputConverter<CapitalWithMoreInfoResponse> parser = new BeanOutputConverter<>(CapitalWithMoreInfoResponse.class);
        String format = parser.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPromptWithMoreInfo);
        Prompt prompt = promptTemplate.create(Map.of(
                "stateOrCountry", question,
                "format", format
        ));
        ChatResponse response = chatModel.call(prompt);

        return Objects.requireNonNull(parser.convert(response.getResult().getOutput().getContent())).toString();
    }

    private String askGeneralQuestion(String question) {
        PromptTemplate promptTemplate = new PromptTemplate(generalQuestionPromptTemplate);
        Map<String, Object> variables = Map.of(
                "question", question //
        );
        Prompt prompt = promptTemplate.create(variables);

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    private void checkPrompt(String text, QuestionTypeEnum questionTypeEnum) {
        int maxQuestionLength = 20;
        if (questionTypeEnum.equals(QuestionTypeEnum.GENERAL_QUESTION)) {
            maxQuestionLength = 50;
        }

        if (text == null || text.trim().isEmpty()) {
            throw new BadRequestException("Question cannot be null or empty");
        }

        if (text.length() > maxQuestionLength) {
            throw new BadRequestException("Question cannot exceed "+ maxQuestionLength + "  characters");
        }

        if (!text.matches("[a-zA-Z0-9áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽ _\\-,.]+")) {
            throw new BadRequestException("Question can only contain alphabets and spaces");
        }

        if (text.toLowerCase().contains("ignore") || text.toLowerCase().contains("bypass") || text.contains("ignoruj")) {
            throw new BadRequestException("Do not use words like ignore or bypass");
        }
    }

}
