package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
          eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN.value(), oneTimeTokenHandler());
        } else {
          throw new RuntimeException(this.getClass().getName() + ": redis connection failed");
        }
      });
  }

  private Handler<Message<Object>> oneTimeTokenHandler() {
    return messageHandler -> {
      System.out.println("Requested oneTimeTokenHandler message: " + messageHandler.body());

      RedisAPI redis = RedisAPI.api(redisClient);
      redis.get("lastAccess", res -> {
        if (res.succeeded()) {
          String lastAccessString;
          if (res.result() != null) {
            lastAccessString = res.result().toString();
            System.out.println("lastAccess:" + lastAccessString);
          } else {
            lastAccessString = "0";
            System.out.println("lastAccess is null -> set zero");
          }

          redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
            if (setres.succeeded()) {
              messageHandler.reply("Requested oneTimeTokenHandler message: " + lastAccessString + " : " + messageHandler.body());
            } else {
              throw new RuntimeException("redis setex failed.");
            }
          });

        } else {
          throw new RuntimeException("redis get failed.");
        }
      });


    };
  }


/*
  private Handler<RoutingContext> getTopRoutingHandler() {
    return routingContext -> {

      EventBus eventBus = vertx.eventBus();
      String message = routingContext.request().getParam("id");
      eventBus.request(ApiguardEventBusNames.DECRYPT.value(), message, reply -> {
        if (reply.succeeded()) {
          String replyMessage = reply.result().body().toString();
          RedisAPI redis = RedisAPI.api(redisClient);
          redis.get("lastAccess", res -> {
            if (res.succeeded()) {
              String lastAccessString;
              if (res.result() != null) {
                lastAccessString = res.result().toString();
                System.out.println("lastAccess:" + lastAccessString);
              } else {
                lastAccessString = "0";
                System.out.println("lastAccess is null -> set zero");
              }

              redis.setex("lastAccess", "10", Long.toString(System.currentTimeMillis()), setres -> {
                if (setres.succeeded()) {
                  // This handler will be called for every request
                  HttpServerResponse response = routingContext.response();
                  response.putHeader("content-type", "text/plain");
                  // Write to the response and end it
                  response.end("Hello World from Vert.x-Web! :" + lastAccessString);
                } else {
                  throw new RuntimeException("redis setex failed.");
                }
              });

            } else {
              throw new RuntimeException("redis get failed.");
            }
          });
        } else {
          throw new RuntimeException("eventbus decrypt request failed.");
        }
      });
    };
  }

  private Handler<RoutingContext> getLoginRoutingHandler() {
    return routingContext -> {
      // This handler will be called for every request
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain");

      // Write to the response and end it
      response.end("Login Handler from Vert.x-Web!");
    };
  }
*/
}


