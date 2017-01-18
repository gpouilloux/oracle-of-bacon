package com.serli.oracle.of.bacon.repository;

import com.google.gson.JsonElement;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Suggest;
import io.searchbox.core.SuggestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> getActorsSuggests(String suggestQuery) {
        String query = "{\n" +
                "\"actor-suggestion\": {\n" +
	                "    \"text\":\"" + suggestQuery + "\",\n" +
		            "    \"term\": {\n" +
		            "       \"field\": \"name\"\n" +
		            "       }\n" +
	                "    }\n" +
                    "}";

	    Suggest suggest = new Suggest.Builder(query)
			    .addIndex("movies")
			    .addType("actor")
			    .build();


	    ArrayList<String> list = new ArrayList<>();
        try {
            SuggestResult result = jestClient.execute(suggest);
	        return result.getSuggestions("actor-suggestion")
			        .stream()
			        .map(s -> s.text)
			        .collect(Collectors.toList());
//            result.getJsonObject().get("hits").getAsJsonObject().get("hits")
//                    .getAsJsonArray().forEach(jsonElement -> {
//                list.add(jsonElement.getAsJsonObject().get("_source").getAsJsonObject().get("name").getAsString());
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
