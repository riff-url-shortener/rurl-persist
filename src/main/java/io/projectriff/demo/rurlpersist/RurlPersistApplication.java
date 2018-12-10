package io.projectriff.demo.rurlpersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
public class RurlPersistApplication {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Bean
	public StringRedisTemplate stringRedisTemplate() {
		return new StringRedisTemplate(redisConnectionFactory());
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		// TODO parameterize this
		return new LettuceConnectionFactory(new RedisStandaloneConfiguration(
				"my-redis-master.default.svc.cluster.local"));
	}


	@Bean
	public Consumer<String> persistUrl() {
		return s -> {
			System.out.println("Got URL:"+s);
			// input is of the form hash:originalUrl
			redisTemplate.opsForValue().set(getHash(s), getOriginalUrl(s));
		};
	}

    private String getOriginalUrl(String input) {
        int splitIndex = input.indexOf(':');
        if (splitIndex == input.length() + 1 ) {
            // delimiter not found, or delimiter is last
            throw new IllegalArgumentException("expected form key:value but was " + input);
        }
        return input.substring(splitIndex + 1);
    }

    private String getHash(String input) {
        int splitIndex = input.indexOf(':');
        if (splitIndex < 0) {
            // delimiter not found, or delimiter is last
            throw new IllegalArgumentException("expected form key:value but was " + input);
        }
        return input.substring(0, splitIndex);
    }

	public static void main(String[] args) {
		SpringApplication.run(RurlPersistApplication.class, args);
	}
}
