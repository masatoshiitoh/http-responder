package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.RedisOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

public class OnetimeTokenVerticle extends AbstractVerticle {
  private RedisOptions redisOptions = new RedisOptions();
  private Redis redisClient;

  @Override
  public void start(Promise<Void> promise) throws Exception {
    Redis.createClient(vertx, new io.vertx.redis.client.RedisOptions())
      .connect(onConnect -> {
        if (onConnect.succeeded()) {
          redisClient = onConnect.result();

          EventBus eventBus = vertx.eventBus();
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), oneTimeTokenResetHandler());
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), oneTimeTokenVerifyHandler());
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), oneTimeTokenUpdateHandler());
          promise.complete();
          return;
        } else {
          promise.fail(this.getClass().getName() + ": redis connection failed");
          return;
        }
      });
  }

  private Handler<Message<Object>> oneTimeTokenResetHandler() {
    return messageHandler -> {
      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) {
          messageHandler.fail(1, "oneTimeTokenResetHandler: redis get failed.");
          return;
        }

        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) {
            messageHandler.fail(1, "oneTimeTokenResetHandler: redis setex failed.");
            return;
          }

          messageHandler.reply("Requested oneTimeTokenResetHandler message: " + lastAccessString + " : " + messageHandler.body());
          return;
        });
      });
    };
  }

  private Handler<Message<Object>> oneTimeTokenVerifyHandler() {
    return messageHandler -> {
      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) {
          messageHandler.fail(1, "oneTimeTokenVerifyHandler: redis get failed.");
          return;
        }
        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) {
            messageHandler.fail(1, "oneTimeTokenVerifyHandler: redis setex failed.");
            return;
          }
          messageHandler.reply("Requested oneTimeTokenVerifyHandler message: " + lastAccessString + " : " + messageHandler.body());
          return;
        });
      });
    };
  }


  private Handler<Message<Object>> oneTimeTokenUpdateHandler() {
    return messageHandler -> {
      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.failed()) {
          messageHandler.fail(1, "oneTimeTokenUpdateHandler: redis get failed.");
          return;
        }

        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }

        redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
          if (setres.failed()) {
            messageHandler.fail(1, "oneTimeTokenUpdateHandler: redis setex failed.");
            return;
          }

          messageHandler.reply("Requested oneTimeTokenUpdateHandler message: " + lastAccessString + " : " + messageHandler.body());
          return;
        });
      });
    };
  }
}


