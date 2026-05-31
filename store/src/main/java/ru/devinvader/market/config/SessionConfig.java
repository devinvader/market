package ru.devinvader.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration
@EnableRedisWebSession(redisNamespace = "market:session")
public class SessionConfig {
}
