package com.serli.oracle.of.bacon.repository;

import com.google.gson.JsonElement;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchRepository {

    private final JestClient jestClient;

    public ElasticSearchRepository() {
        jestClient = createClient();

    }

    public static JestClient createClient() {
        JestClient jestClient;
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200")
                .multiThreaded(true)
                .readTimeout(60000)
                .build());

        jestClient = factory.getObject();
        return jestClient;
    }

    public static List<String> getActorsSuggests(String searchQuery) throws IOException {
        String query = "{\n" +
                "    \"size\": 5,\n" +
                "    \"query\": {\n" +
                "            \"match\" : {\n" +
                "                    \"name\" : \"*" + searchQuery + "*\"\n" +
                "            }\n" +
                "    }\n" +
                "}";

        Search search = new Search.Builder(query)
                .addIndex("movies")
                .addType("actor")
                .build();
        JestClient jestClient = createClient();
        SearchResult result = jestClient.execute(search);
        ArrayList<String> list = new ArrayList<>();
        result.getJsonObject().get("hits").getAsJsonObject().get("hits")
        .getAsJsonArray().forEach(jsonElement -> {
            list.add(jsonElement.getAsJsonObject().get("_source").getAsJsonObject().get("name").toString());
        });
        return list;
    }
}
