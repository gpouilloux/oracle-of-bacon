package com.serli.oracle.of.bacon.repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class MongoDbRepository {

    private final MongoClient mongoClient;

    public MongoDbRepository() {
        mongoClient = new MongoClient("localhost", 27017);
    }

    public Optional<Document> getActorByName(String name) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
        MongoCollection mongoCollection = mongoDatabase.getCollection("actors");
        MongoCursor<Document> mongoCursor = mongoCollection.find(eq("name:ID", name)).iterator();

        if (mongoCursor.hasNext()) {
            Document actor = mongoCursor.next();
            return Optional.of(actor);
        }
        return Optional.empty();
    }
}
