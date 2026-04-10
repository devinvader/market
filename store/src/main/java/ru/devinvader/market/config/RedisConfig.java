package ru.devinvader.market.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.devinvader.market.utils.CacheConstants;
import ru.devinvader.market.domain.Item;
import ru.devinvader.market.service.dto.ListQueryResult;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> marketStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Item> itemRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Item> serializer = new Jackson2JsonRedisSerializer<>(Item.class);
        RedisSerializationContext<String, Item> context = RedisSerializationContext
                .<String, Item>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, ListQueryResult> itemListRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<ListQueryResult> serializer = new Jackson2JsonRedisSerializer<>(
                ListQueryResult.class);
        RedisSerializationContext<String, ListQueryResult> context = RedisSerializationContext
                .<String, ListQueryResult>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public CommandLineRunner initRedis(ReactiveRedisTemplate<String, String> marketStringRedisTemplate) {
        return args -> {
            marketStringRedisTemplate.opsForValue().setIfAbsent(CacheConstants.LIST_VERSION_KEY, "1").subscribe();
        };
    }
}
