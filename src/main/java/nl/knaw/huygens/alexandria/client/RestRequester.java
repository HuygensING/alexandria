package nl.knaw.huygens.alexandria.client;

import java.time.Duration;
import java.time.Instant;

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
  Map<Status, Function<Response, RestResult<T>>> statusMappers = new HashMap<>();
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
