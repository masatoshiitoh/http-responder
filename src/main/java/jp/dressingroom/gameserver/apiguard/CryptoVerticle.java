package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class CryptoVerticle extends AbstractVerticle {
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();

    eventBus.consumer(ApiguardEventBusNames.ENCRYPT.value(), getEncryptMessageHandler());
    eventBus.consumer(ApiguardEventBusNames.DECRYPT.value(), getDecryptMessageHandler());
    startPromise.complete();
  }

  private Handler<Message<Object>> getDecryptMessageHandler() {
    // this is decrypter
    return messageHandler -> {
      messageHandler.reply("Decrypted => " + messageHandler.body());
    };
  }

  private Handler<Message<Object>> getEncryptMessageHandler() {
    // this is encrypter
    return messageHandler -> {
      messageHandler.reply("Encrypted => " + messageHandler.body());
    };
  }


}
