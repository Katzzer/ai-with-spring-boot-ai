package com.pavelkostal.aiwithjava.utils;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.pavelkostal.aiwithjava.model.PromptDBItem;
import com.pavelkostal.aiwithjava.model.QuestionTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
public class CosmoDB {

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;
    private final CosmosProperties cosmosProperties;

    public void savePromptDataToDB(PromptDBItem promptDBItem) {

        CompletableFuture.runAsync(() -> {
            try {
                saveDataToDB(promptDBItem);
            } catch (Exception e) {
                log.error("Error occurred while saving prompt data: {}", e.getMessage());
            }
        });
    }
    public void saveDataToDB(PromptDBItem promptDBItem) {
        intiDb();

        log.info("Saving prompt to DB: {}", promptDBItem);
        CosmosItemResponse<PromptDBItem> item = container.createItem(promptDBItem, new PartitionKey(promptDBItem.partitionKey()), new CosmosItemRequestOptions());
        log.info("Created item with request charge of {} within duration {}",
                item.getRequestCharge(),
                item.getDuration());
    }

    public ArrayList<PromptDBItem> readItemsFromDB(QuestionTypeEnum questionTypeEnum) {
        intiDb();

        CosmosPagedIterable<PromptDBItem> items = container.readAllItems(
                new PartitionKey(questionTypeEnum.getQuestionType()),
                PromptDBItem.class
        );

        ArrayList<PromptDBItem> itemList = new ArrayList<>();
        items.forEach(itemList::add);
        return itemList;
    }

    private void intiDb() {
        ArrayList<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("West US");

        //  Create sync client
        client = new CosmosClientBuilder()
                .endpoint(cosmosProperties.getDatabase())
                .key(cosmosProperties.getKey())
                .preferredRegions(preferredRegions)
                .userAgentSuffix("CosmosDBJavaAIPromptDemo")
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        try {
            createDatabaseIfNotExists();
            createContainerIfNotExists();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void createDatabaseIfNotExists() {
        String databaseName = "AI_Prompt";
        System.out.println("Create database " + databaseName + " if not exists.");

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() {
        String containerName = "ListOfPrompts";
        System.out.println("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/partitionKey");

        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

        System.out.println("Checking container " + container.getId() + " completed!\n");
    }

}
