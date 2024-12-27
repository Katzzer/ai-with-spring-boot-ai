package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.exceptionHandling.BadRequestException;
import com.pavelkostal.aiwithjava.model.*;
import com.pavelkostal.aiwithjava.utils.CosmoDB;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final SimpleVectorStore vectorStore;
    private final CosmoDB cosmoDB;

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
        questionFromWeb.setQuestionTypeEnum(questionTypeEnum);
        logQuestion(questionFromWeb);
        checkPrompt(questionFromWeb);

        return switch (questionTypeEnum) {
            case GENERAL_QUESTION -> askGeneralQuestion(questionFromWeb);
            case FILM_QUESTION, UHK_DOCUMENTATION -> getDataFromVectorStore(questionFromWeb);
            case CAPITAL_CITY_QUESTION -> getCapitalOfCountry(questionFromWeb);
            case CAPITAL_CITY_WITH_MORE_INFO_QUESTION -> getCapitalOfCountryWithMoreInfo(questionFromWeb);
        };
    }

    private String getDataFromVectorStore(QuestionFromWeb questionFromWeb) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .query(questionFromWeb.getQuestion()).withTopK(4));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        // only for debugging
        contentList.forEach(System.out::println);

        Resource resource = movieRagPromptTemplate;
        if (questionFromWeb.getQuestionTypeEnum().equals(QuestionTypeEnum.UHK_DOCUMENTATION)) {
            resource = uhkPromptTemplate;
        }

        PromptTemplate promptTemplate = new PromptTemplate(resource);
        Prompt prompt = promptTemplate.create(Map.of(
                "input", questionFromWeb,
                "documents", String.join("\n", contentList)));

        ChatResponse response = chatModel.call(prompt);

        saveCompletedPromptDataToDB(questionFromWeb, response, prompt);
        logPrompt(response);
        return response.getResult().getOutput().getContent();
    }

    private String getCapitalOfCountry(QuestionFromWeb questionFromWeb) {

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPrompt);
        Prompt prompt = promptTemplate.create(Map.of(
                "stateOrCountry", questionFromWeb.getQuestion()));

        ChatResponse response = chatModel.call(prompt);

        saveCompletedPromptDataToDB(questionFromWeb, response, prompt);
        logPrompt(response);
        return response.getResult().getOutput().getContent();
    }

    private String getCapitalOfCountryWithMoreInfo(QuestionFromWeb questionFromWeb) {
        BeanOutputConverter<CapitalWithMoreInfoResponse> parser = new BeanOutputConverter<>(CapitalWithMoreInfoResponse.class);
        String format = parser.getFormat();

        PromptTemplate promptTemplate = new PromptTemplate(getCapitalPromptWithMoreInfo);
        Prompt prompt = promptTemplate.create(Map.of(
                "stateOrCountry", questionFromWeb.getQuestion(),
                "format", format
        ));
        ChatResponse response = chatModel.call(prompt);

        saveCompletedPromptDataToDB(questionFromWeb, response, prompt);
        logPrompt(response);
        return Objects.requireNonNull(parser.convert(response.getResult().getOutput().getContent())).toString();
    }

    private String askGeneralQuestion(QuestionFromWeb questionFromWeb) {
        PromptTemplate promptTemplate = new PromptTemplate(generalQuestionPromptTemplate);
        Map<String, Object> variables = Map.of(
                "question", questionFromWeb.getQuestion() //
        );
        Prompt prompt = promptTemplate.create(variables);

        ChatResponse response = chatModel.call(prompt);

        saveCompletedPromptDataToDB(questionFromWeb, response, prompt);
        logPrompt(response);
        return response.getResult().getOutput().getContent();
    }

    private void checkPrompt(QuestionFromWeb questionFromWeb) {
        String question = questionFromWeb.getQuestion();

        int maxQuestionLength = 30;
        if (questionFromWeb.getQuestionTypeEnum().equals(QuestionTypeEnum.GENERAL_QUESTION) ||
            questionFromWeb.getQuestionTypeEnum().equals(QuestionTypeEnum.FILM_QUESTION)) {
            maxQuestionLength = 50;
        }

        if (question == null || question.trim().isEmpty()) {
            String errorMessage = "Question cannot be null or empty";
            saveInvalidPromptDataToDB(questionFromWeb, errorMessage);
            throw new BadRequestException(errorMessage);
        }

        if (question.length() > maxQuestionLength) {
            String errorMessage = "Question cannot exceed " + maxQuestionLength + "  characters";
            saveInvalidPromptDataToDB(questionFromWeb, errorMessage);
            throw new BadRequestException(errorMessage);
        }

        if (!question.matches("[a-zA-Z0-9áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽ _\\-,.']+")) {
            String errorMessage = "Question can only contain alphabets and spaces";
            saveInvalidPromptDataToDB(questionFromWeb, errorMessage);
            throw new BadRequestException(errorMessage);
        }

        if (question.toLowerCase().contains("ignore") || question.toLowerCase().contains("bypass") || question.toLowerCase().contains("ignoruj")) {
            String errorMessage = "Do not use words like ignore or bypass";
            saveInvalidPromptDataToDB(questionFromWeb, errorMessage);
            throw new BadRequestException(errorMessage);
        }
    }

    private void logQuestion(QuestionFromWeb questionFromWeb) {
        log.info("Question: {}, | Question typ: {}", questionFromWeb.getQuestion(), questionFromWeb.getQuestionTypeEnum().getQuestionType());
    }

    private void logPrompt(ChatResponse response) {
        log.info("Response: {}", response.getResult().getOutput().getContent());
    }

    private void saveCompletedPromptDataToDB(QuestionFromWeb questionFromWeb, ChatResponse response, Prompt prompt) {
        PromptDBItem promptDBItem = new PromptDBItem(
                questionFromWeb.getQuestionTypeEnum().getQuestionType() + System.currentTimeMillis(),
                questionFromWeb.getQuestionTypeEnum().getQuestionType(),
                response.getMetadata().getModel(),
                questionFromWeb.getQuestion(),
                prompt.getContents(),
                response.getResult().getOutput().getContent(),
                response.getMetadata().getUsage().getPromptTokens(),
                response.getMetadata().getUsage().getGenerationTokens(),
                response.getMetadata().getUsage().getTotalTokens(),
                false,
                null,
                LocalDateTime.now()
        );

        cosmoDB.savePromptDataToDB(promptDBItem);
    }

    private void saveInvalidPromptDataToDB(QuestionFromWeb questionFromWeb, String errorMessage) {
        PromptDBItem promptDBItem = new PromptDBItem(
                questionFromWeb.getQuestionTypeEnum().getQuestionType() + System.currentTimeMillis(),
                questionFromWeb.getQuestionTypeEnum().getQuestionType(),
                null,
                questionFromWeb.getQuestion(),
                null,
                null,
                null,
                null,
                null,
                true,
                errorMessage,
                LocalDateTime.now()
        );

        cosmoDB.savePromptDataToDB(promptDBItem);
    }

}
