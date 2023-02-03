package org.analyzer.logs.caching;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisCacheConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> userReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper mapper) {
        final StringRedisSerializer keySerializer = new StringRedisSerializer();
        final GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(
                createObjectMapper(), JacksonObjectReader.create(), JacksonObjectWriter.create()
        );
        final RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        final RedisSerializationContext<String, Object> context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    private ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(mapper, null);

        StdTypeResolverBuilder typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.EVERYTHING, mapper.getPolymorphicTypeValidator());
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);

        mapper.setDefaultTyping(typer);
        return mapper;
    }
}
