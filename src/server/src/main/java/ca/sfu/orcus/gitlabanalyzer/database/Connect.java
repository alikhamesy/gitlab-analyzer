package ca.sfu.orcus.gitlabanalyzer.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class Connect {
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(System.getenv("MONGO_URI"))) {
            MongoDatabase TestDB = mongoClient.getDatabase("Test");
            MongoCollection<Document> repoCacheCollection = TestDB.getCollection("RepoCache");

            List<Document> users = repoCacheCollection.find(eq("item", "user")).into(new ArrayList<>());
            System.out.println("\n\n\n\n\n\nHello Mongo");
            for (Document user : users) {
                System.out.println(user);
            }
            System.out.println("Goodbye Mongo\n\n\n\n");
        }
    }
}
