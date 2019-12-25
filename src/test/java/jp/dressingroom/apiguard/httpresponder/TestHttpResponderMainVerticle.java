package jp.dressingroom.apiguard.httpresponder;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestHttpResponderMainVerticle {

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    System.setProperty("server.port", "18888");

    vertx.deployVerticle(new HttpResponderMainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticleDeployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
  @Test
  void httpResponderGetHelloResponse(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(18888, "localhost", "/hello")
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

    client.post(18888, "localhost", "/hello")
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

    client.post(18888, "localhost", "/test/path")
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

    client.get(18888, "localhost", "/test/path")
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

    client.get(18888, "localhost", "/404")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 404);
        testContext.completeNow();
      })));
  }
  @Test
  void httpResponderGet500Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.get(18888, "localhost", "/500")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 500);
        testContext.completeNow();
      })));
  }
  @Test
  void httpResponderPost404Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.post(18888, "localhost", "/404")
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

    client.post(18888, "localhost", "/500")
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
    client.request(HttpMethod.OPTIONS, 18888, "localhost", "/test/path")
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

    client.request(HttpMethod.OPTIONS, 18888, "localhost", "/404")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 404);
        testContext.completeNow();
      })));
  }
  @Test
  void httpResponderOption500Response(Vertx vertx, VertxTestContext testContext) throws Throwable {
    WebClient client = WebClient.create(vertx);

    client.request(HttpMethod.OPTIONS, 18888, "localhost", "/500")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertTrue(response.statusCode() == 500);
        testContext.completeNow();
      })));
  }


}
