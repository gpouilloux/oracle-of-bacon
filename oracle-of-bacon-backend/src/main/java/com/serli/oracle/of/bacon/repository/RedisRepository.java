package com.serli.oracle.of.bacon.repository;

import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisRepository {
    private final Jedis driver;

    private final String SEARCH_ACTOR_KEY = "searches_actor";

    public RedisRepository() {
        driver = new Jedis("localhost", 6379);
    }

    public List<String> getLastTenSearches() {
        // implement last 10 searchs
        return driver.lrange(SEARCH_ACTOR_KEY, 0, 9);
    }

    public void appendActorSearch(String searchActor) {
        driver.lpush(SEARCH_ACTOR_KEY, searchActor);
    }
}
