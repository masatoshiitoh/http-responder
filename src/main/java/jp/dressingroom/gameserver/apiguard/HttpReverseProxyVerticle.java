package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisOptions;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

import java.util.List;

public class HttpReverseProxyVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // you must prepare redis to start MainVerticle
          HttpServer server = vertx.createHttpServer();
          Router router = Router.router(vertx);
          router.get("/").handler(getTopRoutingHandler());
          router.get("/login").handler(getLoginRoutingHandler());
          server.requestHandler(router).listen(8888);
  }

  private Handler<RoutingContext> getTopRoutingHandler() {
    return routingContext -> {

      EventBus eventBus = vertx.eventBus();

      String requestParamId = routingContext.request().getParam("id");

      eventBus.request(ApiguardEventBusNames.DECRYPT.value(), requestParamId, decrypted -> {
        if (decrypted.failed()) throw new RuntimeException("eventbus decrypt request failed.");

        // verify token
        String decryptedMessage = decrypted.result().body().toString();
        eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), decryptedMessage, verifyOnetimeTokenReply ->{
          if (verifyOnetimeTokenReply.failed()) throw new RuntimeException("eventbus onetimeToken request failed.");

          String lastAccessString = verifyOnetimeTokenReply.result().body().toString();
          String responseBodyString = "Hello World from Vert.x-Web! id=" + requestParamId + " :" + lastAccessString;

          // encrypt response body payload.
          eventBus.request(ApiguardEventBusNames.ENCRYPT.value(), responseBodyString, encrypted -> {
            if (encrypted.failed()) throw new RuntimeException("eventbus encrypt request failed.");

            // build response body
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");
            // Write to the response and end it
            response.end(encrypted.result().body().toString());
          });
        });
      });
    };
  }

  private Handler<RoutingContext> getLoginRoutingHandler() {
    return routingContext -> {
      // This handler will be called for every request

      EventBus eventBus = vertx.eventBus();
      String message = routingContext.request().getParam("id");
      eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), message, resetOnetimeTokenReply -> {
        // guard
        if (resetOnetimeTokenReply.failed()) throw new RuntimeException("eventbus resetOnetimeToken request failed.");

        // build response body
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        // Write to the response and end it
        response.end("Login Handler from Vert.x-Web!");
        });
    };
  }
}
