package jp.dressingroom.gameserver.apiguard.entity;

public class RawMessage {
  byte[] value;

  public byte[] getValue() {
    return value;
  }
  public int length() { return value.length; }
  public RawMessage(byte[] value) {
    this.value = value;
  }
}
