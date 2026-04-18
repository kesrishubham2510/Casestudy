package com.myreflectionthoughts.covidstat.service.impl.cacheservice;

import com.myreflectionthoughts.covidstat.config.CacheConfig;
import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.ICache;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.function.Supplier;


// For the current use-case, I'm using String as key
@Service
public class RedisCacheService implements ICache<String, String> {

    private int port;
    private String hostname;
    private final Logger logger;
    private final Jedis jedis;
    private final CacheConfig cacheConfig;
    private final Tracer tracer;

    public RedisCacheService(CacheConfig cacheConfig, Tracer tracer){
        this.cacheConfig = cacheConfig;
        this.tracer = tracer;
        logger = LoggerFactory.getLogger(RedisCacheService.class);
        init();
        jedis = new Jedis(hostname, port);
        ping();
    }

    @Override
    public void init() {
        // Logic to initialise the connection properties
        this.hostname = cacheConfig.getHost();
        this.port = cacheConfig.getPort();
    }

    @Override
    public void ping() {
        inSpan("redis.ping", "server-ping", () -> {
            String pingResult = jedis.ping();
            logger.info("Connecting to redis server at host {}, port {}, ping {}", hostname, port, pingResult);

            if(!pingResult.equalsIgnoreCase("PONG")){
                throw new CaseStudyException(ServiceConstant._ERR_CACHE_CONNECTION_KEY, 503, "Could not connect with the redis server");
            }

            return pingResult;
        });
    }

    @Override
    public void put(String key, String value,long expiryTimestamp) {
        inSpan("redis.set", key, () -> {
            SetParams setParams = new SetParams();
            setParams.exAt(expiryTimestamp);
            this.jedis.set(key, value, setParams);
            logger.info("Cache entry cached to redis server successfully");
            return null;
        });
    }

    @Override
    public String get(String key) {
        return inSpan("redis.get", key, () -> {
            String cachedResponse = this.jedis.get(key);
            logger.info("Cache entry retrieved successfully from redis server");
            return cachedResponse;
        });
    }

    private <T> T inSpan(String spanName, String keyTag, Supplier<T> action) {
        Span span = tracer.nextSpan()
                .name(spanName)
                .tag("db.system", "redis")
                .tag("net.peer.name", hostname)
                .tag("key",  keyTag)
                .tag("net.peer.port", String.valueOf(port))
                .start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return action.get();
        } catch (RuntimeException exception) {
            span.error(exception);
            throw exception;
        } finally {
            span.end();
        }
    }
}
