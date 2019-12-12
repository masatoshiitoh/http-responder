package jp.dressingroom.gameserver.apiguard.verticle;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;


public class HttpReverseProxyVerticle extends AbstractVerticle {
  WebClient client;
  String proxyHost;
  String proxyUserAgent;
  int proxyPort;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    proxyUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36";
    proxyHost = "yahoo.co.jp";
    proxyPort = 80;

    WebClientOptions webClientOptions = new WebClientOptions();
    webClientOptions.setUserAgent(proxyUserAgent);
    client = WebClient.create((Vertx) vertx, webClientOptions);

    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    Route bodyLessRoute = router.route()
      .method(HttpMethod.GET)
      .method(HttpMethod.HEAD)
      .method(HttpMethod.OPTIONS)
      .method(HttpMethod.DELETE);
    Route bodiedRoute = router.route()
      .method(HttpMethod.POST)
      .method(HttpMethod.PUT)
      .method(HttpMethod.PATCH);

    // route catches all methods and paths.
    bodyLessRoute.handler(bodyLessProxyHandler());
    bodiedRoute.handler(bodiedProxyHandler());
    server.requestHandler(router).listen(8888);

    startPromise.complete();
  }

  private Handler<RoutingContext> bodyLessProxyHandler() {
    return routingContext -> {
      String requestUri = routingContext.request().absoluteURI();
      String query = routingContext.request().query();
      MultiMap headers = routingContext.request().headers();
      HttpMethod method = routingContext.request().method();

      RequestOptions requestOptions = new RequestOptions();
      headers.entries().forEach(s -> requestOptions.addHeader(s.getKey(), s.getValue()));
      requestOptions.setHost(proxyHost);
      requestOptions.setURI("/");
      requestOptions.setSsl(false);
      requestOptions.setPort(80);

      System.out.println(method.name() + " " + requestOptions.toJson().toString());
      System.out.println("Dump headers: " + requestOptions.getHeaders().toString());

      client
        .request(method, requestOptions).ssl(false).send(
        ar -> {
          if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            HttpServerResponse proxyResponse = routingContext.response();
            proxyResponse.end((io.vertx.core.buffer.Buffer) response.body());
          } else {
            sendResponse(routingContext, HttpStatusCodes.INTERNAL_SERVER_ERROR);
          }
        }
      );
    };
  }


  private Handler<RoutingContext> bodiedProxyHandler() {
    return routingContext -> {
      routingContext.request().bodyHandler(bodiedProxyHandler -> {
          sendResponse(routingContext, HttpStatusCodes.OK, "called bodied with " + routingContext.request().method().name());
        }
      );

/*
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
*/
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
