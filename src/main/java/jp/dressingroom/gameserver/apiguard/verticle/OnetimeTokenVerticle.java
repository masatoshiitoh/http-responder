package jp.dressingroom.gameserver.apiguard.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import jp.dressingroom.gameserver.apiguard.domain.OnetimeToken;
import jp.dressingroom.gameserver.apiguard.entity.RequestedPayload;
import jp.dressingroom.gameserver.apiguard.entity.RequestedToken;
import jp.dressingroom.gameserver.apiguard.entity.UserId;

public class OnetimeTokenVerticle extends AbstractVerticle {
  private OnetimeToken onetimeToken;

  public OnetimeTokenVerticle(OnetimeToken onetimeToken) {
    this.onetimeToken = onetimeToken;
  }

  @Override
  public void start(Promise<Void> promise) throws Exception {
    EventBus eventBus = vertx.eventBus();

    onetimeToken.registerResetNewTokenHandler(eventBus);
    onetimeToken.registerLookupTokenHandler(eventBus);
    onetimeToken.registerUpdateToken(eventBus);
  }

  public void resetNewToken(UserId userid) {
    EventBus eventBus = vertx.eventBus();
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_RESET.value(), userid, res -> {
      if (res.succeeded()) {

      } else {
        // failed

      }
    });
  }

  public void lookupToken(RequestedToken requestedToken) {
    EventBus eventBus = vertx.eventBus();
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_VERIFY.value(), requestedToken, res -> {});

  }

  public void updateToken(RequestedPayload requestedPayload) {
    EventBus eventBus = vertx.eventBus();
    eventBus.request(ApiguardEventBusNames.ONETIME_TOKEN_UPDATE.value(), requestedPayload, res -> {});
  }
}
