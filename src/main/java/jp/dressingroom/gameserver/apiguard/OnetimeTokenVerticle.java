package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.RedisOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

public class OnetimeTokenVerticle extends AbstractVerticle {
  private RedisOptions redisOptions = new RedisOptions();
  private Redis redisClient;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Redis.createClient(vertx, new io.vertx.redis.client.RedisOptions())
      .connect(onConnect -> {
        if (onConnect.succeeded()) {
          redisClient = onConnect.result();

          EventBus eventBus = vertx.eventBus();
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), oneTimeTokenResetHandler());
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), oneTimeTokenVerifyHandler());
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), oneTimeTokenUpdateHandler());
        } else {
          throw new RuntimeException(this.getClass().getName() + ": redis connection failed");
        }
      });
  }

  private Handler<Message<Object>> oneTimeTokenResetHandler() {
    return messageHandler -> {
      System.out.println("Requested oneTimeTokenResetHandler message: " + messageHandler.body());

      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) throw new RuntimeException("oneTimeTokenResetHandler: redis get failed.");

        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) throw new RuntimeException("oneTimeTokenResetHandler: redis setex failed.");

          messageHandler.reply("Requested oneTimeTokenResetHandler message: " + lastAccessString + " : " + messageHandler.body());
        });
      });
    };
  }

  private Handler<Message<Object>> oneTimeTokenVerifyHandler() {
    return messageHandler -> {
      System.out.println("Requested oneTimeTokenVerifyHandler message: " + messageHandler.body());

      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) throw new RuntimeException("oneTimeTokenVerifyHandler: redis get failed.");

        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) throw new RuntimeException("oneTimeTokenVerifyHandler: redis setex failed.");

          messageHandler.reply("Requested oneTimeTokenVerifyHandler message: " + lastAccessString + " : " + messageHandler.body());
        });
      });
    };
  }


  private Handler<Message<Object>> oneTimeTokenUpdateHandler() {
    return messageHandler -> {
      System.out.println("Requested oneTimeTokenUpdateHandler message: " + messageHandler.body());

      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) throw new RuntimeException("oneTimeTokenUpdateHandler: redis get failed.");

        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) throw new RuntimeException("oneTimeTokenUpdateHandler: redis setex failed.");

          messageHandler.reply("Requested oneTimeTokenUpdateHandler message: " + lastAccessString + " : " + messageHandler.body());
        });
      });
    };
  }
}


