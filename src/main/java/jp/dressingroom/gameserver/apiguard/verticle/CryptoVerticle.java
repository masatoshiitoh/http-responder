package jp.dressingroom.gameserver.apiguard.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import jp.dressingroom.gameserver.apiguard.entity.RawMessage;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * handle encoded request payload.
 * crypt: AES, 256bit, CBC, preset IV, pre shared key.
 * user must define IV and pre shared key to users server and client.
 *
 */
public class CryptoVerticle extends AbstractVerticle {
  private Cipher encryptor;
  private Cipher decryptor;

  String IV =  "1234567890abcdef";
  String PSK = "1234567890abcdef";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ApiguardEventBusNames.ENCRYPT.value(), encryptMessageHandler());
    eventBus.consumer(ApiguardEventBusNames.DECRYPT.value(), decryptMessageHandler());

    System.out.println("IV getbytes length :" + IV.getBytes(StandardCharsets.US_ASCII).length);

    IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.US_ASCII));
    SecretKeySpec key = new SecretKeySpec(PSK.getBytes(StandardCharsets.US_ASCII), "AES");
    encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encryptor.init(Cipher.ENCRYPT_MODE, key, iv);
    decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    decryptor.init(Cipher.DECRYPT_MODE, key, iv);

    startPromise.complete();
  }

  private Handler<Message<RawMessage>> decryptMessageHandler() {
    // this is decrypter
    return messageHandler -> {
      cryptWork(decryptor, messageHandler.body(), messageHandler);
    };
  }

  private Handler<Message<RawMessage>> encryptMessageHandler() {
    // this is encrypter
    return messageHandler -> {
      cryptWork(encryptor, messageHandler.body(), messageHandler);
    };
  }

  private void cryptWork(Cipher cipher, RawMessage rawMessage, Message<RawMessage> message) {
    try {
      byte[] rb = cipher.doFinal(rawMessage.getValue());
      message.reply(new RawMessage(rb));
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
      message.fail(1,"IllegalBlockSizeException");
    } catch (BadPaddingException e) {
      e.printStackTrace();
      message.fail(1,"BadPaddingException");
    }
  }
}
