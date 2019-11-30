package jp.dressingroom.gameserver.apiguard.domain;

import jp.dressingroom.gameserver.apiguard.entity.Payload;
import jp.dressingroom.gameserver.apiguard.entity.Token;
import jp.dressingroom.gameserver.apiguard.entity.Uuid;

public interface OnetimeToken {
  int resetNewToken(Uuid uuid);
  int lookupToken(Uuid uuid, Token token);
  int updateToken(Uuid uuid, Token token, Payload payload);
}
