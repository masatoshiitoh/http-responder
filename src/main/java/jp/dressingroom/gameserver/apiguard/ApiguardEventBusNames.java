package jp.dressingroom.gameserver.apiguard;

public enum ApiguardEventBusNames {
  ENCRYPT("eccrypt"),
  DECRYPT("decrypt"),
  HTTP_REVERSE_PROXY("httpReverseProxy"),
  ONETIME_TOKEN("onetimeToken"),
  ;

  private final String text;

  private ApiguardEventBusNames(final String text) {
    this.text = text;
  }

  public String value() {
    return this.text;
  }
}
