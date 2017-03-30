package nl.knaw.huygens.alexandria.dropwizard.api;

public class AboutInfo {
  String appName;
  String startedAt;
  String version;

  public AboutInfo setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getAppName() {
    return appName;
  }

  public AboutInfo setStartedAt(String startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public AboutInfo setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

}