package jp.dressingroom.gameserver.apiguard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
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
  KeyGenerator keyGenerator;
  Key KEY;

  String IV = "12345667890abcdefghijklmnopqrstuvwxyz";
  String PSK = "YourPreSharedKeyMustChangeFromThisOne";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer(ApiguardEventBusNames.ENCRYPT.value(), getEncryptMessageHandler());
    eventBus.consumer(ApiguardEventBusNames.DECRYPT.value(), getDecryptMessageHandler());




    encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    encryptor.init(Cipher.ENCRYPT_MODE, KEY);

    decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
    decryptor.init(Cipher.DECRYPT_MODE, KEY);

    base64decoder = Base64.getDecoder();
    base64encoder = Base64.getEncoder();

    keyGenerator = KeyGenerator.getInstance("AES");
    SecretKey secretKey = keyGenerator.generateKey();

    startPromise.complete();
  }

  private Handler<Message<Object>> getDecryptMessageHandler() {
    // this is decrypter
    return messageHandler -> {
      byte[] rawEncrypted = base64decoder.decode((byte[]) messageHandler.body());
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
