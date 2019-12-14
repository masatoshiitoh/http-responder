package jp.dressingroom.apiguard.httpresponder.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Base64;


public class HttpServerVerticle extends AbstractVerticle {
  String br;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
    configRetriever.getConfig(json -> {
      JsonObject result = json.result();
      Integer port = result.getInteger("server.port");
      br = result.getString("line.separator");

      HttpServer server = vertx.createHttpServer();
      Router router = Router.router(vertx);
      Route route = router.route();

      // route catches all methods and paths.
      route.handler(bodiedProxyHandler());
      server.requestHandler(router).listen(port);

      startPromise.complete();
    });
  }

  private Handler<RoutingContext> bodiedProxyHandler() {
    return routingContext -> {
      routingContext.request().bodyHandler(bodiedProxyHandler -> {
          Base64.Encoder encoder = Base64.getEncoder();
          sendResponse(routingContext, HttpStatusCodes.OK,
            "method: " + routingContext.request().method().name() + br +
              "absoluteUri: " + routingContext.request().absoluteURI() + br +
              "headers: " + routingContext.request().headers().toString() + br +
              "payload(Base64): " + encoder.encodeToString(bodiedProxyHandler.getBytes()) + br +
              "payload(raw): " + new String(bodiedProxyHandler.getBytes())
          );
        }
      );
    };
  }

  /**
   * send response to requester with status
   *
   * @param routingContext
   * @param status
   */
  private void sendResponse(RoutingContext routingContext, HttpStatusCodes status) {
    sendResponse(routingContext, status, null);
  }

  /**
   * @param routingContext
   * @param status
   * @param message
   */
  private void sendResponse(RoutingContext routingContext, HttpStatusCodes status, String message) {
    HttpServerResponse response = routingContext.response();
    response.setStatusCode(status.value());
    if (message == null) {
      response.end();
    } else {
      response.end(message);
    }
  }
}