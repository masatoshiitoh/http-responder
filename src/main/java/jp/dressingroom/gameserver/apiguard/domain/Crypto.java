package jp.dressingroom.gameserver.apiguard.domain;

import jp.dressingroom.gameserver.apiguard.entity.Payload;

public interface Crypto {
  Payload encrypt(Payload payload);
  Payload decrypt(Payload payload);
}
