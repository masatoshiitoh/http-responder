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
      String message = routingContext.request().getParam("id");
      eventBus.request(ApiguardEventBusNames.DECRYPT.value(), message, reply -> {
        if (reply.succeeded()) {
          String replyMessage = reply.result().body().toString();
          String lastAccessString;
          lastAccessString = replyMessage;
          System.out.println("lastAccess:" + lastAccessString);

          // This handler will be called for every request
          HttpServerResponse response = routingContext.response();
          response.putHeader("content-type", "text/plain");
          // Write to the response and end it
          response.end("Hello World from Vert.x-Web! id=" + message + " :" + lastAccessString);

        } else {
          throw new RuntimeException("eventbus decrypt request failed.");
        }
      });
    };
  }

  private Handler<RoutingContext> getLoginRoutingHandler() {
    return routingContext -> {
      // This handler will be called for every request

      EventBus eventBus = vertx.eventBus();
      String message = routingContext.request().getParam("id");
      eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN.value(), message, loginOnetimeReply -> {
        if (loginOnetimeReply.succeeded()) {
          HttpServerResponse response = routingContext.response();
          response.putHeader("content-type", "text/plain");

          // Write to the response and end it
          response.end("Login Handler from Vert.x-Web!");
        } else {
          throw new RuntimeException("eventbus onetimeToken request failed.");
        }
        });


    };
  }
}
