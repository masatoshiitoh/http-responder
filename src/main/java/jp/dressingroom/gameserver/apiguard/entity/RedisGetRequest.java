package jp.dressingroom.gameserver.apiguard.entity;

public class RedisGetRequest {
  String key;

  public RedisGetRequest(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
