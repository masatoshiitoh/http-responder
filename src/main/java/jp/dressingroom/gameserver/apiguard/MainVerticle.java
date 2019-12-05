package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import jp.dressingroom.gameserver.apiguard.verticle.impl.OnetimeTokenRedisImpl;
import jp.dressingroom.gameserver.apiguard.verticle.OnetimeTokenVerticle;


public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.verticle.service.RedisVerticle", res -> {
      if (res.failed()) { startPromise.fail("RedisVerticle start failed: " + res.cause());}
    });
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.verticle.HttpReverseProxyVerticle", res -> {
      if (res.failed()) { startPromise.fail("HttpReverseProxyVerticle start failed: " + res.cause());}
    });
    vertx.deployVerticle("jp.dressingroom.gameserver.apiguard.verticle.CryptoVerticle", res -> {
      if (res.failed()) { startPromise.fail("CryptoVerticle start failed: " + res.cause());}
    });
    OnetimeTokenVerticle v = new OnetimeTokenVerticle(new OnetimeTokenRedisImpl());
    vertx.deployVerticle(v, res -> {
      if (res.failed()) { startPromise.fail("OnetimeTokenVerticle start failed: " + res.cause());}
    });
    // delay parameter 5000 means 5,000 milliseconds( = 5sec).
    // vertx.setPeriodic(5000, id -> {System.out.println("timer fired on MainVerticle");});

  }

//  @Override
//  public void stop() {
//  }
}
