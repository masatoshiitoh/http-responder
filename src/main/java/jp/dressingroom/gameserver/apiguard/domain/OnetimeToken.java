package jp.dressingroom.gameserver.apiguard.domain;

import io.vertx.core.eventbus.EventBus;
import jp.dressingroom.gameserver.apiguard.entity.*;

public interface OnetimeToken {

  void registerResetNewTokenHandler(EventBus eventBus);
  void resetNewToken(EventBus eventBus, UserId userId);

  void registerLookupTokenHandler(EventBus eventBus);
  void lookupToken(EventBus eventBus, RequestedToken token);

  void registerUpdateToken(EventBus eventBus);
  void updateToken(EventBus eventBus, RequestedPayload payload);
}


