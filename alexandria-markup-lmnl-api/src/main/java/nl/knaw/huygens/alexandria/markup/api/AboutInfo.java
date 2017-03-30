package nl.knaw.huygens.alexandria.markup.api;

/*
 * #%L
 * alexandria-markup-lmnl-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


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