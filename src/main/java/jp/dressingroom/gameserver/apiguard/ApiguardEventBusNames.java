package jp.dressingroom.gameserver.apiguard;

public enum ApiguardEventBusNames {
  ENCRYPT("payload.encrypt"),
  DECRYPT("payload.decrypt"),
  // HTTP_REVERSE_PROXY("http.reverseProxy"),
  ONETIME_TOKEN_RESET("onetimeToken.reset"),
  ONETIME_TOKEN_VERIFY("onetimeToken.verify"),
  ONETIME_TOKEN_UPDATE("onetimeToken.update"),
  ;

  private final String text;

  private ApiguardEventBusNames(final String text) {
    this.text = text;
  }

  public String value() {
    return this.text;
  }
}
