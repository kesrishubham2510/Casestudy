package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.logging.Logger;


// For the current use-case, I'm using String as key
@Service
public class RedisCacheService implements ICache<String, String> {

    private int port;
    private String hostname;
    private final Logger logger;
    private final Jedis jedis;

    public RedisCacheService(){
        logger = Logger.getLogger(RedisCacheService.class.getSimpleName());
        init();
        jedis = new Jedis(hostname, port);
        ping();
    }

    @Override
    public void init() {
        // Logic to initialise the connection properties
        this.hostname = "localhost";
        this.port = 6379;
    }

    @Override
    public void ping() {
        // Logic to connect to the cache using the connection properties
        String pingResult = jedis.ping();
        logger.info("Connecting to redis server at, host:- "+hostname+", port:- "+port+", ping:- "+jedis.ping());

        if(!pingResult.equalsIgnoreCase("PONG")){
            throw new CaseStudyException(ServiceConstant._ERR_CACHE_CONNECTION_KEY, 503, "Could not connect with the redis server");
        }
    }

    @Override
    public void put(String key, String value, long expiryTimestamp) {
        SetParams setParams = new SetParams();
        setParams.exAt(expiryTimestamp);
        this.jedis.set(key, value, setParams);
        logger.info("Cache | Key:- "+key+" cached to redis server successfully");
    }

    @Override
    public String get(String key) {
        String cachedResponse = this.jedis.get(key);
        logger.info("Cache | Key:- "+key+" retrieved successfully from redis server");
        return cachedResponse;
    }
}
