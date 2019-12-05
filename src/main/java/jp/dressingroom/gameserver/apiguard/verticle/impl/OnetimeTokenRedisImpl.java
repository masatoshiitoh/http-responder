package jp.dressingroom.gameserver.apiguard.verticle.impl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import jp.dressingroom.gameserver.apiguard.domain.OnetimeToken;
import jp.dressingroom.gameserver.apiguard.entity.*;
import jp.dressingroom.gameserver.apiguard.verticle.ApiguardEventBusNames;

public class OnetimeTokenRedisImpl implements OnetimeToken {
  public OnetimeTokenRedisImpl() {

  }

  // うーん、routingContextをもらえば全部出来るんだけど、それってただ「切り出しただけ」なんだよなーーー
  // それでもいいっちゃいいんだけどなーー
  // Promiseとかfutureを返すようにするべきなんかなー



  @Override
  public void registerResetNewTokenHandler(EventBus eventBus) {
    eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), oneTimeTokenResetHandler(eventBus));
  }

  @Override
  public void resetNewToken(EventBus eventBus, UserId userId) {
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), userId, res -> {
      //
    });
  }

  @Override
  public void registerLookupTokenHandler(EventBus eventBus) {
    eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), oneTimeTokenVerifyHandler(eventBus));
  }

  @Override
  public void lookupToken(EventBus eventBus, RequestedToken token) {
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), token, res -> {});
  }

  @Override
  public void registerUpdateToken(EventBus eventBus) {
    eventBus.consumer(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), oneTimeTokenUpdateHandler(eventBus));
  }

  @Override
  public void updateToken(EventBus eventBus, RequestedPayload payload) {
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), payload, res -> {});
  }

  // UserIDに紐付いたUUIDをリセットし、あたらしいUUIDを払い出す
  private Handler<Message<UserId>> oneTimeTokenResetHandler(EventBus eventBus) {
    return messageHandler -> {
      UserId userId = messageHandler.body();
      RedisGetRequest redisGetRequest = new RedisGetRequest(userId.getValue());

      eventBus.request(ApiguardEventBusNames.REDIS_GET.value(), redisGetRequest, res -> {
        // guards.
        if (res.failed()) {
          messageHandler.fail(1, "redis verticle reply failed.");
          return;
        }

        // main.
        String lastAccessString;
        if (res.result() != null) {
          lastAccessString = res.result().toString();
        } else {
          lastAccessString = "null";
        }
        eventBus.request(
          ApiguardEventBusNames.REDIS_SETEX.value(),
          new RedisSetExRequest("key", "value", 10),
          setex -> {
            if (setex.failed()) {
              messageHandler.fail(1, "oneTimeTokenResetHandler: redis setex failed.");
              return;
            }
            messageHandler.reply("Requested oneTimeTokenResetHandler message: " + lastAccessString + " : " + messageHandler.body());
          });
      });

    };
  }


  // UserIDに紐付いたOnetime token (UUID) を取得し、
  // 前回Onetime tokenだったらキャッシュを返却
  // 今回Onetime tokenだったらVerifiledを返却(これを受けて、呼び出し元は実APIを呼び出すはず）
  // どちらでもなければ NotFound を返却（これを受けて、呼び出し元はエラーを返すはず）
  private Handler<Message<RequestedToken>> oneTimeTokenVerifyHandler(EventBus eventBus) {
    return messageHandler -> {
      RequestedToken requestedToken = messageHandler.body();
    };
  }


  // UserID とトークンとペイロードを受け取って、更新処理をおこなう
  // トークンがnextでなければ黙って成功を返す。内容は廃棄（前回トークンの更新処理が再度来た可能性があるので、失敗では無く、うまくいったと思わせる必要がある＿
  // トークンがnextなら、nextを今回に、レスポンスキャッシュを更新、トークンを払い出す、トークンをローテート更新して、新しい状態を返却する
  private Handler<Message<RequestedPayload>> oneTimeTokenUpdateHandler(EventBus eventBus) {
    return messageHandler -> {
    };
  }

}
