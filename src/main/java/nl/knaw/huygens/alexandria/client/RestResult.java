package nl.knaw.huygens.alexandria.client;

import java.time.Duration;

/*
 * #%L
 * alexandria-java-client
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

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import nl.knaw.huygens.alexandria.api.model.ErrorEntity;

public class RestResult<T> {
  private boolean failure = false;
  private T cargo;
  private Response response;
  private Exception exception;
  private String errorMessage;
  private Duration turnaroundTime;

  public static <T> RestResult<T> failingResult(Response response) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setResponse(response);
    if (response.hasEntity()) {
      try {
        ErrorEntity errorEntity = response.readEntity(ErrorEntity.class);
        result.setErrorMessage(errorEntity.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public static <T> RestResult<T> failingResult(Exception exception) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setException(exception);
    return result;
  }

  public static <T> RestResult<T> failingResult(String errorMessage) {
    RestResult<T> result = new RestResult<>();
    result.setFail(true);
    result.setErrorMessage(errorMessage);
    return result;
  }

  public RestResult<T> setCargo(T cargo) {
    this.cargo = cargo;
    return this;
  }

  public T get() {
    return cargo;
  }

  public void setFail(boolean failure) {
    this.failure = failure;
  }

  public boolean hasFailed() {
    return failure;
  }

  void setResponse(Response response) {
    this.response = response;
  }

  public Optional<Response> getResponse() {
    return Optional.ofNullable(response);
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public Optional<Exception> getException() {
    return Optional.ofNullable(exception);
  }

  private void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Optional<String> getErrorMessage() {
    return Optional.ofNullable(errorMessage);
  }

  public Optional<String> getFailureCause() {
    String cause = null;

    if (errorMessage != null) {
      cause = errorMessage;

    } else if (exception != null) {
      cause = exception.getMessage();

    } else if (response != null) {
      cause = "Unexpected return status: " + response.getStatus() + " " + response.getStatusInfo().toString();

    }
    return Optional.ofNullable(cause);
  }

  public Duration getTurnaroundTime() {
    return turnaroundTime;
  }

  public RestResult<T> setTurnaroundTime(Duration processingTime) {
    this.turnaroundTime = processingTime;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }

}
