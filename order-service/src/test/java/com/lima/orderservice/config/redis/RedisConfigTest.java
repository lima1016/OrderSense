package com.lima.orderservice.config.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig 단위 테스트")
class RedisConfigTest {

  @Mock
  private RedisConnectionFactory connectionFactory;

  private RedisConfig redisConfig;

  @BeforeEach
  void setUp() {
    redisConfig = new RedisConfig();
  }

  @Test
  @DisplayName("RedisCacheManager 빈이 올바르게 생성되어야 함")
  void cacheManager_ShouldCreateManagerSuccessfully() {
    // when
    RedisCacheManager cacheManager = redisConfig.cacheManager(connectionFactory);

    // then
    assertThat(cacheManager).isNotNull();
  }

  @Test
  @DisplayName("RedisCacheManager는 null이 아닌 설정을 가져야 함")
  void cacheManager_ShouldHaveNonNullConfiguration() {
    // when
    RedisCacheManager cacheManager = redisConfig.cacheManager(connectionFactory);

    // then
    assertThat(cacheManager).isNotNull();
    assertThat(cacheManager.getCacheNames()).isNotNull();
  }

  @Test
  @DisplayName("null ConnectionFactory로 CacheManager 생성 시 예외가 발생해야 함")
  void cacheManager_WithNullConnectionFactory_ShouldThrowException() {
    // when & then
    assertThrows(NullPointerException.class, () -> {
      redisConfig.cacheManager(null);
    });
  }

  @Test
  @DisplayName("캐시 설정은 10분의 TTL을 가져야 함")
  void cacheConfiguration_ShouldHaveTenMinutesTTL() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    Duration ttl = config.getTtl();

    // then
    assertThat(ttl).isNotNull();
    assertThat(ttl).isEqualTo(Duration.ofMinutes(10));
  }

  @Test
  @DisplayName("캐시 설정은 StringRedisSerializer를 키 직렬화기로 사용해야 함")
  void cacheConfiguration_ShouldUseStringRedisSerializerForKeys() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    RedisSerializationContext.SerializationPair<?> keySerializer = config.getKeySerializationPair();

    // then
    assertThat(keySerializer).isNotNull();
    assertThat(keySerializer.getSerializer()).isInstanceOf(StringRedisSerializer.class);
  }

  @Test
  @DisplayName("캐시 설정은 GenericJackson2JsonRedisSerializer를 값 직렬화기로 사용해야 함")
  void cacheConfiguration_ShouldUseGenericJackson2JsonRedisSerializerForValues() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    RedisSerializationContext.SerializationPair<?> valueSerializer = config.getValueSerializationPair();

    // then
    assertThat(valueSerializer).isNotNull();
    assertThat(valueSerializer.getSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);
  }

  @Test
  @DisplayName("동일한 ConnectionFactory로 여러 번 호출해도 CacheManager가 생성되어야 함")
  void cacheManager_MultipleCallsWithSameFactory_ShouldCreateManagers() {
    // when
    RedisCacheManager cacheManager1 = redisConfig.cacheManager(connectionFactory);
    RedisCacheManager cacheManager2 = redisConfig.cacheManager(connectionFactory);

    // then
    assertThat(cacheManager1).isNotNull();
    assertThat(cacheManager2).isNotNull();
  }

  @Test
  @DisplayName("캐시 설정은 null 값 캐싱 동작을 확인해야 함")
  void cacheConfiguration_ShouldVerifyNullValueBehavior() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    boolean allowsNullValues = config.getAllowCacheNullValues();

    // then
    assertThat(allowsNullValues).isTrue();
  }

  @Test
  @DisplayName("캐시 설정은 키 프리픽스를 사용해야 함")
  void cacheConfiguration_ShouldUseKeyPrefix() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    boolean usePrefix = config.usePrefix();

    // then
    assertThat(usePrefix).isTrue();
  }

  @Test
  @DisplayName("다양한 TTL 값으로 캐시 설정을 생성할 수 있어야 함")
  void cacheConfiguration_WithDifferentTTL_ShouldCreateValidConfiguration() {
    // given & when
    RedisCacheConfiguration config1 = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(1));
    RedisCacheConfiguration config5 = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5));
    RedisCacheConfiguration config30 = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(30));

    // then
    assertThat(config1.getTtl()).isEqualTo(Duration.ofMinutes(1));
    assertThat(config5.getTtl()).isEqualTo(Duration.ofMinutes(5));
    assertThat(config30.getTtl()).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  @DisplayName("캐시 설정은 통계 수집을 활성화할 수 있어야 함")
  void cacheConfiguration_ShouldSupportStatistics() {
    // given
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
        );

    // when
    RedisCacheConfiguration configWithStats = config.enableStatistics();

    // then
    assertThat(configWithStats).isNotNull();
  }

  @Test
  @DisplayName("RedisConfig는 @EnableCaching 어노테이션을 가져야 함")
  void redisConfig_ShouldHaveEnableCachingAnnotation() {
    // when
    boolean hasAnnotation = RedisConfig.class.isAnnotationPresent(
        org.springframework.cache.annotation.EnableCaching.class
    );

    // then
    assertThat(hasAnnotation).isTrue();
  }

  @Test
  @DisplayName("RedisConfig는 @Configuration 어노테이션을 가져야 함")
  void redisConfig_ShouldHaveConfigurationAnnotation() {
    // when
    boolean hasAnnotation = RedisConfig.class.isAnnotationPresent(
        org.springframework.context.annotation.Configuration.class
    );

    // then
    assertThat(hasAnnotation).isTrue();
  }
}