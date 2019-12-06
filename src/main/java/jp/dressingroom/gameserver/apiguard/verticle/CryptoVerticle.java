package jp.dressingroom.gameserver.apiguard.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * handle encoded request payload.
 * crypt: AES, 256bit, CBC, preset IV, pre shared key.
 * user must define IV and pre shared key to users server and client.
 *
 */
public class CryptoVerticle extends AbstractVerticle {
  private Cipher encryptor;
  private Cipher decryptor;
  private Base64.Decoder base64decoder;
  private Base64.Encoder base64encoder;

  String IV =  "1234567890abcdef";
  String PSK = "1234567890abcdef";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ApiguardEventBusNames.ENCRYPT.value(), getEncryptMessageHandler());
    eventBus.consumer(ApiguardEventBusNames.DECRYPT.value(), getDecryptMessageHandler());

    System.out.println("IV getbytes length :" + IV.getBytes(StandardCharsets.US_ASCII).length);

    IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.US_ASCII));
    SecretKeySpec key = new SecretKeySpec(PSK.getBytes(StandardCharsets.US_ASCII), "AES");
    encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encryptor.init(Cipher.ENCRYPT_MODE, key, iv);
    decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    decryptor.init(Cipher.DECRYPT_MODE, key, iv);

    base64decoder = Base64.getDecoder();
    base64encoder = Base64.getEncoder();

    startPromise.complete();
  }

  private Handler<Message<Object>> getDecryptMessageHandler() {
    // this is decrypter
    return messageHandler -> {
      if (messageHandler.body().toString().length() > 0) {
        System.out.println("request body length :" + messageHandler.body().toString().length());
        System.out.println("request body :" + messageHandler.body());

        byte[] rawEncrypted = base64decoder.decode(messageHandler.body().toString().getBytes());
        try {

          byte[] decoded = decryptor.doFinal(rawEncrypted);
          messageHandler.reply(decoded);

        } catch (IllegalBlockSizeException e) {
          e.printStackTrace();
          messageHandler.fail(1,"IllegalBlockSizeException");
        } catch (BadPaddingException e) {
          e.printStackTrace();
          messageHandler.fail(1,"BadPaddingException");
        }

      } else {
        messageHandler.reply(null);
      }
    };
  }

  private Handler<Message<Object>> getEncryptMessageHandler() {
    // this is encrypter
    return messageHandler -> {
      Object rawOriginalPayload = messageHandler.body();
      byte[] rawEncrypted = new byte[0];
      try {

        rawEncrypted = encryptor.doFinal((byte[]) rawOriginalPayload);
        String base64Encoded = base64encoder.encodeToString(rawEncrypted);
        messageHandler.reply(base64Encoded);

      } catch (IllegalBlockSizeException e) {
        e.printStackTrace();
        messageHandler.fail(1,"IllegalBlockSizeException");
      } catch (BadPaddingException e) {
        e.printStackTrace();
        messageHandler.fail(1,"BadPaddingException");
      }
    };
  }


}
