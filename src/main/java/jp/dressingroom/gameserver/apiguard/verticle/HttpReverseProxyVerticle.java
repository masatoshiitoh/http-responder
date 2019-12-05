package jp.dressingroom.gameserver.apiguard.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;

public class HttpReverseProxyVerticle extends AbstractVerticle {
  WebClient client;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    // you must prepare redis to start MainVerticle
    HttpServer server = vertx.createHttpServer();
    client = WebClient.create((Vertx) vertx);

    Router router = Router.router(vertx);
    router.get("/").handler(getTopRoutingHandler());
    router.get("/login").handler(getLoginRoutingHandler());
    router.post("/api").handler(postApiRoutingHandler());
    server.requestHandler(router).listen(8888);

    startPromise.complete();
  }

  private Handler<RoutingContext> getSimpleProxyHandler() {
    return routingContext -> {


    };
  }

  private Handler<RoutingContext> getTopRoutingHandler() {
    return routingContext -> {

      EventBus eventBus = vertx.eventBus();
      String requestParamId = routingContext.request().getParam("id");

      eventBus.request(ApiguardEventBusNames.DECRYPT.value(), requestParamId, decrypted -> {
        if (decrypted.failed()) {
          sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
          return;
        }

        // verify token
        String decryptedMessage = decrypted.result().body().toString();
        eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), decryptedMessage, verifyOnetimeTokenReply -> {
          if (verifyOnetimeTokenReply.failed()) {
            sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
            return;
          }

          String responseBodyString = "Hello World from Vert.x-Web! id=" + requestParamId
            + " :" + verifyOnetimeTokenReply.result().body().toString();

          // TODO: place proxy call here
          // if 5xx error returns, DO NOT UPDATE onetime token!!!!
          // because when you update with error reeturn, user client will get "error result" with current token.

          // call proxy

          // check result

          // update onetime token with proxy response.
          eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), responseBodyString, updateOnetimeTokenReply -> {
            if (updateOnetimeTokenReply.failed()) {
              sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
              return;
            }

            String updatedResponseBodyString = updateOnetimeTokenReply.result().body().toString();
            // encrypt proxy response body payload.
            eventBus.request(ApiguardEventBusNames.ENCRYPT.value(), updatedResponseBodyString, encrypted -> {
              if (encrypted.failed()) {
                sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
                return;
              }

              // build response body
              HttpServerResponse response = routingContext.response();
              response.putHeader("content-type", "text/plain");
              // Write to the response and end it
              response.end(encrypted.result().body().toString());
            });
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
        if (resetOnetimeTokenReply.failed()) {
          sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
          return;
        }

        // build response body
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        // Write to the response and end it
        response.end("Login Handler from Vert.x-Web!");
        return;
      });
    };
  }

  private Handler<RoutingContext> postApiRoutingHandler() {
    return routingContext -> {
      routingContext.request().bodyHandler(bodyHandler -> {
        byte[] body = bodyHandler.getBytes();
        String id = routingContext.request().getParam("id");

        System.out.println("posted id: " + id + " body: " + new String(body));

        // build response body
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        // Write to the response and end it
        response.end("postApiRoutingHandler received: " + new String(body));
        return;
      });
    };
  }

  /**
   * send response to requester with status
   * @param routingContext
   * @param status
   */
  private void sendResponse(RoutingContext routingContext, HttpStatusCodes status) {
    sendResponse(routingContext, status, null);
  }

  /**
   *
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
