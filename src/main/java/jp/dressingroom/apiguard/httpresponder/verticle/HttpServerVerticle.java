package jp.dressingroom.apiguard.httpresponder.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Counter;
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
      if (json.succeeded()) {
        JsonObject result = json.result();
        Integer port = result.getInteger("server.port");
        br = result.getString("line.separator");

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route("/500").handler(goba -> {
          sendResponse(goba, HttpStatusCodes.INTERNAL_SERVER_ERROR);
        });
        router.route("/400").handler(goba -> {
          sendResponse(goba, HttpStatusCodes.BAD_REQUEST);
        });
        router.route("/404").handler(goba -> {
          sendResponse(goba, HttpStatusCodes.NOT_FOUND);
        });
        router.route("/counter").handler(goba -> {
          vertx.sharedData().getCounter("httpResponderCounter", counterAsyncResult -> {
            if (counterAsyncResult.succeeded()) {
              Counter counter = counterAsyncResult.result();
              counter.incrementAndGet(increments -> {
                if (increments.succeeded()) {
                  Long count = increments.result();
                  sendResponse(goba, HttpStatusCodes.OK, count.toString());
                }
              });
            }
          });
        });

        router.get("/hello").handler(goba -> {
          sendResponse(goba, HttpStatusCodes.OK, "Hello");
        });
        router.post("/hello").handler(goba -> {
          goba.request().bodyHandler(body -> {
            sendResponse(goba, HttpStatusCodes.OK, new String(body.getBytes()));
          });
        });

        // Catch all - methods and paths.
        router.route().handler(bodiedProxyHandler());
        server.requestHandler(router).listen(port);

        startPromise.complete();
      } else {
        startPromise.fail("configuration not found");
      }
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

  private void sendResponse(RoutingContext routingContext, HttpStatusCodes status) {
    sendResponse(routingContext, status, null);
  }

  private void sendResponse(RoutingContext routingContext, HttpStatusCodes status, String message) {
    HttpServerResponse response = routingContext.response();
    response.setStatusCode(status.value());
    response.headers().add("HttpResponder", "true");
    if (message == null) {
      response.end();
    } else {
      response.end(message);
    }
  }
}
