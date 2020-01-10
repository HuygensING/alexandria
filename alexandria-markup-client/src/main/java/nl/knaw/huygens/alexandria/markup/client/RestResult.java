package nl.knaw.huygens.alexandria.markup.client;

import nl.knaw.huygens.alexandria.api.model.ErrorEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Optional;

/*
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
      cause =
          "Unexpected return status: "
              + response.getStatus()
              + " "
              + response.getStatusInfo().toString();
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
