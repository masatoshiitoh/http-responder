package jp.dressingroom.apiguard.httpresponder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;


public class HttpResponderMainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle("jp.dressingroom.apiguard.httpresponder.verticle.HttpServerVerticle", res -> {
      if (res.failed()) {
        startPromise.fail("HttpServerVerticle start failed: " + res.cause());
      } else {
        startPromise.complete();
      }
    });
  }
}
