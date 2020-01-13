package jp.dressingroom.apiguard.httpresponder;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestHttpResponderMainVerticle {

  static int nextPort;

  int responderPort;

  @BeforeAll
  static void initPort() {
    nextPort = 18000;
  }

  static int getPort() {
    return nextPort++;
  }


  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    responderPort = getPort();
    System.setProperty("server.port", String.valueOf(responderPort));

    vertx.deployVerticle(new HttpResponderMainVerticle(), testContext.completing());
  }

  @Test
  void verticleDeployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }

  @Test
  void httpResponderGetHelloResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(responderPort, "localhost", "/hello")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 200);
        assertTrue(response.body().equals("Hello"));
        testContext.completeNow();
      })));
  }

  @Test
  void httpResponderPostHelloResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.post(responderPort, "localhost", "/hello")
      .as(BodyCodec.string())
      .sendBuffer(Buffer.buffer("some payload"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 200);
          assertTrue(response.body().equals("some payload"));
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderPostSomePathResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.post(responderPort, "localhost", "/test/path")
      .as(BodyCodec.string())
      .sendBuffer(Buffer.buffer("some payload"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 200);
          assertTrue(response.body().contains("/test/path"));
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderGetSomePathResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(responderPort, "localhost", "/test/path")
      .as(BodyCodec.string())
      .send(
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 200);
          assertTrue(response.body().contains("/test/path"));
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderGet404Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(responderPort, "localhost", "/404")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 404);
        testContext.completeNow();
      })));
  }

  @Test
  void httpResponderGet500Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(responderPort, "localhost", "/500")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 500);
        testContext.completeNow();
      })));
  }

  @Test
  void httpResponderPost404Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.post(responderPort, "localhost", "/404")
      .as(BodyCodec.string())
      .sendBuffer(Buffer.buffer("some payload"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 404);
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderPost500Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.post(responderPort, "localhost", "/500")
      .as(BodyCodec.string())
      .sendBuffer(Buffer.buffer("some payload"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 500);
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderOptionSomePathResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);
    client.request(HttpMethod.OPTIONS, responderPort, "localhost", "/test/path")
      .as(BodyCodec.string())
      .send(
        testContext.succeeding(response -> testContext.verify(() -> {
          assertTrue(response.statusCode() == 200);
          assertTrue(response.body().contains("/test/path"));
          testContext.completeNow();
        })));
  }

  @Test
  void httpResponderOption404Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.request(HttpMethod.OPTIONS, responderPort, "localhost", "/404")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 404);
        testContext.completeNow();
      })));
  }

  @Test
  void httpResponderOption500Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.request(HttpMethod.OPTIONS, responderPort, "localhost", "/500")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 500);
        testContext.completeNow();
      })));
  }

  @Test
  void httpResponderGetCounterResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.request(HttpMethod.GET, responderPort, "localhost", "/counter")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 200);
        long counter1 = Long.parseLong(response.body());
        client.request(HttpMethod.GET, responderPort, "localhost", "/counter")
          .as(BodyCodec.string())
          .send(testContext.succeeding(secondCall -> testContext.verify(() -> {
            assertTrue(secondCall.statusCode() == 200);
            long counter2 = Long.parseLong(secondCall.body());
            assertTrue(counter2 > counter1, "counter did not incrementd.  counter1 is " + counter1 + " coutner2 is " + counter2);
            testContext.completeNow();
          })));
      })));
  }
}
