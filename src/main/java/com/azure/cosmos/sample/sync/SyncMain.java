// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.sample.sync;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.sample.common.*;
import com.azure.cosmos.util.CosmosPagedIterable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class SyncMain {

    private CosmosClient client;

    private final String databaseName = "MainDB";
    private final String containerName = "Employee";

    private CosmosDatabase database;
    private CosmosContainer container;

    protected static Logger logger = LoggerFactory.getLogger(SyncMain.class.getSimpleName());

    public void close() {
        client.close();
    }

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        SyncMain p = new SyncMain();

        try {
            logger.info("Starting SYNC main");
            p.getStartedDemo();
            logger.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            logger.error("Cosmos getStarted failed with", e);
        } finally {
            logger.info("Closing the client");
            p.close();
        }
        System.exit(0);
    }

    //  </Main>

    private void getStartedDemo() throws Exception {
        logger.info("Using Azure Cosmos DB endpoint: {}", AccountSettings.HOST);

        //  Create sync client
        //  <CreateSyncClient>
        client = new CosmosClientBuilder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                //  Setting the preferred location to Cosmos DB Account region
                //  West US is just an example. User should set preferred location to the Cosmos DB region closest to the application
                .preferredRegions(Collections.singletonList("West US"))
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        //  </CreateSyncClient>

        createDatabaseIfNotExists();
        createContainerIfNotExists();

        //  Setup family items to create
        ArrayList<Employee> employeesToCreate = new ArrayList<>();
        employeesToCreate.add(Employees.getDeveloperEmployeeItem());
        employeesToCreate.add(Employees.getDevOpsEmployeeItem());
        employeesToCreate.add(Employees.getOperationalEmployeeItem());
        employeesToCreate.add(Employees.getCEOEmployeeItem());

//        createEmployees(employeesToCreate);

        logger.info("Reading items.");
        readItems(employeesToCreate);

        logger.info("Querying items.");
        queryItems();
    }

    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database {} if not exists.", databaseName);

        //  Create database if not exists
        //  <CreateDatabaseIfNotExists>
        CosmosDatabaseResponse cosmosDatabaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(cosmosDatabaseResponse.getProperties().getId());
        //  </CreateDatabaseIfNotExists>

        logger.info("Checking database {} completed!\n", database.getId());
    }

    private void createContainerIfNotExists() throws Exception {
        logger.info("Create container {} if not exists.", containerName);

        //  Create container if not exists
        //  <CreateContainerIfNotExists>
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/lastName");

        //  Create container with 400 RU/s
        CosmosContainerResponse cosmosContainerResponse =
                database.createContainerIfNotExists(containerProperties, ThroughputProperties.createManualThroughput(400));
        container = database.getContainer(cosmosContainerResponse.getProperties().getId());
        //  </CreateContainerIfNotExists>

        logger.info("Checking container {} completed!\n", container.getId());
    }

    private void createEmployees(List<Employee> employees) throws Exception {
        double totalRequestCharge = 0;
        for (Employee employee : employees) {

            //  <CreateItem>
            //  Create item using container that we created using sync client

            //  Use lastName as partitionKey for cosmos item
            //  Using appropriate partition key improves the performance of database operations
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<Employee> item = container.createItem(employee, new PartitionKey(employee.getLastName()), cosmosItemRequestOptions);
            //  </CreateItem>

            //  Get request charge and other properties like latency, and diagnostics strings, etc.
            logger.info("Created item with request charge of {} within duration {}",
                    item.getRequestCharge(), item.getDuration());
            totalRequestCharge += item.getRequestCharge();
        }
        logger.info("Created {} items with total request charge of {}",
                employees.size(),
                totalRequestCharge);
    }

    private void readItems(ArrayList<Employee> employeesToCreate) {
        //  Using partition key for point read scenarios.
        //  This will help fast look up of items because of partition key
//        employeesToCreate.forEach(employee -> {
//            //  <ReadItem>
//            try {
//                CosmosItemResponse<Employee> item = container.readItem(employee.getId(), new PartitionKey(employee.getLastName()), Employee.class);
//                double requestCharge = item.getRequestCharge();
//                Duration requestLatency = item.getDuration();
//                logger.info("Item successfully read with id {} with a charge of {} and within duration {}",
//                        item.getItem().getId(), requestCharge, requestLatency);
//            } catch (CosmosException e) {
//                logger.error("Read Item failed with", e);
//            }
//            //  </ReadItem>
//        });
    }

    private void queryItems() {
        //  <QueryItems>
        // Set some common query options
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //queryOptions.setEnableCrossPartitionQuery(true); //No longer necessary in SDK v4
        //  Set query metrics enabled to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<Employee> employeesPagedIterable = container.queryItems(
                "SELECT * FROM Family", queryOptions, Employee.class);

        employeesPagedIterable.iterableByPage(10).forEach(cosmosItemPropertiesFeedResponse -> {
            logger.info("Got a page of query result with {} items(s) and request charge of {}",
                    cosmosItemPropertiesFeedResponse.getResults().size(), cosmosItemPropertiesFeedResponse.getRequestCharge());

            logger.info("Item Ids {}", cosmosItemPropertiesFeedResponse
                    .getResults()
                    .stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList()));
        });
        //  </QueryItems>
    }
}
