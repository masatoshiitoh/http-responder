package jp.dressingroom.apiguard.httpviewer;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;


public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle("jp.dressingroom.apiguard.httpviewer.verticle.HttpServerVerticle", res -> {
      if (res.failed()) { startPromise.fail("HttpServerVerticle start failed: " + res.cause());}
    });
    startPromise.complete();
  }
}
