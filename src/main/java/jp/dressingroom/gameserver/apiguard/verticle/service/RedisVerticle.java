package jp.dressingroom.gameserver.apiguard.verticle.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import jp.dressingroom.gameserver.apiguard.entity.RedisGetRequest;
import jp.dressingroom.gameserver.apiguard.entity.RedisSetExRequest;
import jp.dressingroom.gameserver.apiguard.verticle.ApiguardEventBusNames;

public class RedisVerticle extends AbstractVerticle {
  Redis redisClient;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ApiguardEventBusNames.REDIS_GET.value(), redisGetHandler());
    eventBus.consumer(ApiguardEventBusNames.REDIS_SETEX.value(), redisSetExHandler());

    Redis.createClient(vertx, new io.vertx.redis.client.RedisOptions())
      .connect(onConnect -> {
        if (onConnect.succeeded()) {
          redisClient = onConnect.result();
          startPromise.complete();
        } else {
          startPromise.fail(this.getClass().getName() + ": redis connection failed");
        }
      });
  }

  // get handler
  private Handler<Message<RedisGetRequest>> redisGetHandler() {
    return messageHandler -> {
      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get( // REPLIES VALUE
        messageHandler.body().getKey(), // KEY
        res -> {
          if (res.succeeded()) {
            messageHandler.reply(res.result());
          } else {
            messageHandler.fail(1, "redisGetHandler: redis get failed.");
          }
        });
    };
  }

  // setex handler
  private Handler<Message<RedisSetExRequest>> redisSetExHandler() {
    return messageHandler -> {
      RedisAPI redis = RedisAPI.api(redisClient);
      redis.setex( // REPLIES RESULT (OK|NG)
        messageHandler.body().getKey(), // KEY
        String.valueOf(messageHandler.body().getExpireSeconds()), // Expire seconds
        messageHandler.body().getValue(), // VALUE
        res -> {
          if (res.succeeded()) {
            messageHandler.reply(res.result());
          } else {
            messageHandler.fail(1, "redisSetExHandler: redis setex failed.");
          }
        });
    };
  }

}
