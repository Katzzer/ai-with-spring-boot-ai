package com.pavelkostal.aiwithjava.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pavelkostal.aiwithjava.model.Answer;
import com.pavelkostal.aiwithjava.model.GetCapitalRequest;
import com.pavelkostal.aiwithjava.model.GetCapitalResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public OpenAIServiceImpl(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Value("classpath:templates/get-capital-prompt.st")
    private Resource getCapitalPrompt;

    @Value("classpath:templates/get-capital-with-info.st")
    private Resource getCapitalPromptWithInfo;


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

//        return response.getResult().getOutput().getContent();
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
    public String getAnswer(String question) {
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

//    private Answer mapToAnswer(ChatResponse response) {
//        String responseString;
//        try {
//            JsonNode jsonNode = objectMapper.readTree(response.getResult().getOutput().getContent());
//            responseString = jsonNode.get("answer").asText();
//        } catch (JsonProcessingException e) {
//           throw new RuntimeException(e);
//        }
//        return new Answer(responseString);
//    }
}
