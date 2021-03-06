package com.serli.oracle.of.bacon.api;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import com.serli.oracle.of.bacon.repository.MongoDbRepository;
import com.serli.oracle.of.bacon.repository.Neo4JRepository;
import com.serli.oracle.of.bacon.repository.RedisRepository;
import net.codestory.http.annotations.Get;
import org.bson.Document;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class APIEndPoint {
    private final Neo4JRepository neo4JRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final RedisRepository redisRepository;
    private final MongoDbRepository mongoDbRepository;

    public APIEndPoint() {
        neo4JRepository = new Neo4JRepository();
        elasticSearchRepository = new ElasticSearchRepository();
        redisRepository = new RedisRepository();
        mongoDbRepository = new MongoDbRepository();
    }

    private String stringifyPath(Path path, boolean first) {
        String content = first ? "": ",";

        content += stringifyNode(path.start());
        content += "," + stringifyNode(path.end());

        Iterator<Node> itNodes = path.nodes().iterator();
        while (itNodes.hasNext()) {
            content += "," + stringifyNode(itNodes.next());
        }
        Iterator<Relationship> itRelationships = path.relationships().iterator();
        while (itRelationships.hasNext()) {
            content += "," + stringifyRelationship(itRelationships.next());
        }

        return content;
    }

    private String stringifyRelationship(Relationship relationship) {
        String content = "{\"data\":{";

        if (relationship.type() == null) {
            content += "\"target\":" + relationship.startNodeId() + ",";
            content += "\"source\":" + relationship.endNodeId() + ",";
        } else {
            content += "\"source\":" + relationship.startNodeId() + ",";
            content += "\"target\":" + relationship.endNodeId() + ",";
        }
        content += "\"id\":" + relationship.id() + ",";
        content += "\"value\":\"PLAYED_IN\"";

        content += "}}";
        return content;
    }

    private String stringifyNode(Node node) {
        String content = "{\"data\":{";
        content += "\"id\":" + node.id() + ",";
        Iterator<String> str = node.labels().iterator();
        String nodeType = str.next();
        String nodeValue = "";
        switch (nodeType) {
            case "Movie":
                nodeValue = node.get("title").asString();
                break;
            case "Actor":
                nodeValue = node.get("name").asString();
                break;
            default:
                break;
        }
        content += "\"type\":\"" + nodeType + "\",";
        content += "\"value\":\"" + nodeValue + "\"";
        content += "}}";

        return content;
    }

    @Get("bacon-to?actor=:actorName")
    public String getConnectionsToKevinBacon(String actorName) {
        redisRepository.appendActorSearch(actorName);

        List<Path> list = neo4JRepository.getConnectionsToKevinBacon(actorName);
        String content = "[";
        boolean first = true;

        for (int i = 0 ; i < list.size() ; i ++) {
            content += stringifyPath(list.get(i), first);
            if (first) {
                first = false;
            }
        }

        content += "]";
        return content;
//        return "[\n" +
//                "{\n" +
//                "\"data\": {\n" +
//                "\"id\": 85449,\n" +
//                "\"type\": \"Actor\",\n" +
//                "\"value\": \"Bacon, Kevin (I)\"\n" +
//                "}\n" +
//                "},\n" +
//                "{\n" +
//                "\"data\": {\n" +
//                "\"id\": 2278636,\n" +
//                "\"type\": \"Movie\",\n" +
//                "\"value\": \"Mystic River (2003)\"\n" +
//                "}\n" +
//                "},\n" +
//                "{\n" +
//                "\"data\": {\n" +
//                "\"id\": 1394181,\n" +
//                "\"type\": \"Actor\",\n" +
//                "\"value\": \"Robbins, Tim (I)\"\n" +
//                "}\n" +
//                "},\n" +
//                "{\n" +
//                "\"data\": {\n" +
//                "\"id\": 579848,\n" +
//                "\"source\": 85449,\n" +
//                "\"target\": 2278636,\n" +
//                "\"value\": \"PLAYED_IN\"\n" +
//                "}\n" +
//                "},\n" +
//                "{\n" +
//                "\"data\": {\n" +
//                "\"id\": 9985692,\n" +
//                "\"source\": 1394181,\n" +
//                "\"target\": 2278636,\n" +
//                "\"value\": \"PLAYED_IN\"\n" +
//                "}\n" +
//                "}\n" +
//                "]";
    }

    @Get("suggest?q=:searchQuery")
    public List<String> getActorSuggestion(String searchQuery) {
//        return Arrays.asList("Niro, Chel",
//                "Senanayake, Niro",
//                "Niro, Juan Carlos",
//                "de la Rua, Niro",
//                "Niro, Simão");
        return elasticSearchRepository.getActorsSuggests(searchQuery);
    }

    @Get("last-searches")
    public List<String> last10Searches() {
//        return Arrays.asList("Peckinpah, Sam",
//                "Robbins, Tim (I)",
//                "Freeman, Morgan (I)",
//                "De Niro, Robert",
//                "Pacino, Al (I)");
        return redisRepository.getLastTenSearches();
    }

    @Get("actor?name=:actorName")
    public String getActorByName(String actorName) {
        Optional<Document> actor = mongoDbRepository.getActorByName(actorName);
        if (actor.isPresent()) {
            return actor.get().toJson();
        } else {
            return "";
        }
//        return "{\n" +
//                "\"_id\": {\n" +
//                "\"$oid\": \"587bd993da2444c943a25161\"\n" +
//                "},\n" +
//                "\"imdb_id\": \"nm0000134\",\n" +
//                "\"name\": \"Robert De Niro\",\n" +
//                "\"birth_date\": \"1943-08-17\",\n" +
//                "\"description\": \"Robert De Niro, thought of as one of the greatest actors of all time, was born in Greenwich Village, Manhattan, New York City, to artists Virginia (Admiral) and Robert De Niro Sr. His paternal grandfather was of Italian descent, and his other ancestry is Irish, German, Dutch, English, and French. He was trained at the Stella Adler Conservatory and...\",\n" +
//                "\"image\": \"https://images-na.ssl-images-amazon.com/images/M/MV5BMjAwNDU3MzcyOV5BMl5BanBnXkFtZTcwMjc0MTIxMw@@._V1_UY317_CR13,0,214,317_AL_.jpg\",\n" +
//                "\"occupation\": [\n" +
//                "\"actor\",\n" +
//                "\"producer\",\n" +
//                "\"soundtrack\"\n" +
//                "]\n" +
//                "}";
    }
}
