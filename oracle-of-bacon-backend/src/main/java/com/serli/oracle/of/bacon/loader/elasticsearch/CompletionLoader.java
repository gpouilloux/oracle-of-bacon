package com.serli.oracle.of.bacon.loader.elasticsearch;

import com.serli.oracle.of.bacon.repository.ElasticSearchRepository;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletionLoader {
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Expecting 1 arguments, actual : " + args.length);
            System.err.println("Usage : completion-loader <actors file path>");
            System.exit(-1);
        }

        String inputFilePath = args[0];
        JestClient client = ElasticSearchRepository.createClient();

        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(inputFilePath))) {
            Bulk.Builder builder = new Bulk.Builder().defaultIndex("movies").defaultType("actor");
            bufferedReader.lines()
                    .forEach(line -> {
                        count.incrementAndGet();
                        String source = "{\"name\":" + line + ", {\"suggest\": {\"input\": [" + line + "], \"output\":" + line + " }}}";
                        Index index = new Index.Builder(source).build();
                        builder.addAction(index);
//                        System.out.println(line);
                    });
            try {
                client.execute(builder.build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Inserted total of " + count.get() + " actors");
    }
}
