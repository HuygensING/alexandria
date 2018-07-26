package nl.knaw.huygens.alexandria.markup.client;

import java.time.Duration;
import java.time.Instant;

/*
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class RestRequester<T> {
  private int retries = 5;
  private Supplier<Response> responseSupplier;
  final Map<Status, Function<Response, RestResult<T>>> statusMappers = new HashMap<>();
  private Function<Response, RestResult<T>> defaultMapper = RestResult::failingResult;

  public static <T> RestRequester<T> withResponseSupplier(Supplier<Response> responseSupplier) {
    RestRequester<T> requester = new RestRequester<>();
    requester.responseSupplier = responseSupplier;
    return requester;
  }

  public RestRequester<T> onStatus(Status status, Function<Response, RestResult<T>> mapper) {
    statusMappers.put(status, mapper);
    return this;
  }

  public RestRequester<T> onOtherStatus(Function<Response, RestResult<T>> defaultMapper) {
    this.defaultMapper = defaultMapper;
    return this;
  }

  public RestResult<T> getResult() {
    int attempt = 0;
    Response response = null;
    Instant start = Instant.now();
    while (response == null && attempt < retries) {
      attempt++;
      try {
        response = responseSupplier.get();

      } catch (ProcessingException pe) {
        pe.printStackTrace();

      } catch (Exception e) {
        e.printStackTrace();
        return timed(RestResult.failingResult(e), start);
      }
    }
    if (response == null) {
      return timed(RestResult.failingResult("No response from server after " + retries + " attempts."), start);
    }

    Status status = Status.fromStatusCode(response.getStatus());

    if (statusMappers.containsKey(status)) {
      RestResult<T> timed = timed(statusMappers.get(status).apply(response), start);
      timed.setResponse(response);
      return timed;

    } else {
      RestResult<T> timed = timed(defaultMapper.apply(response), start);
      timed.setResponse(response);
      return timed;
    }

  }

  private RestResult<T> timed(RestResult<T> restResult, Instant start) {
    return restResult.setTurnaroundTime(timeSince(start));
  }

  private Duration timeSince(Instant start) {
    return Duration.between(start, Instant.now());
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }
}
