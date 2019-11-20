package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;

import static io.netty.handler.codec.AsciiHeadersEncoder.NewlineType.CRLF;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.HttpReverseProxyVerticle");
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.CryptoVerticle");
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.OnetimeTokenVerticle");

    // delay parameter 5000 means 5,000 milliseconds( = 5sec).
    vertx.setPeriodic(5000, id -> {System.out.println("timer fired on MainVerticle");});

  }

  @Override
  public void stop() {
  }
}
