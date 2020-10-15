package nl.knaw.huygens.alexandria.markup.client

/*
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.function.Supplier
import javax.ws.rs.ProcessingException
import javax.ws.rs.core.Response

typealias ResponseMapper<T> = (Response) -> RestResult<T>

class RestRequester<T> {
    private val statusMappers: MutableMap<Response.Status, ResponseMapper<T>> = EnumMap(javax.ws.rs.core.Response.Status::class.java)
    private var retries = 5
    private var responseSupplier: Supplier<Response>? = null
    private var defaultMapper = { response: Response -> RestResult.failingResult<T>(response) }

    fun onStatus(status: Response.Status, mapper: ResponseMapper<T>): RestRequester<T> {
        statusMappers[status] = mapper
        return this
    }

    fun onOtherStatus(defaultMapper: ResponseMapper<T>): RestRequester<T> {
        this.defaultMapper = defaultMapper
        return this
    }

    val result: RestResult<T>
        get() {
            var attempt = 0
            var response: Response? = null
            val start = Instant.now()
            while (response == null && attempt < retries) {
                attempt++
                try {
                    response = responseSupplier!!.get()
                } catch (pe: ProcessingException) {
                    pe.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return timed(RestResult.failingResult(e), start)
                }
            }
            if (response == null) {
                return timed(
                        RestResult.failingResult("No response from server after $retries attempts."),
                        start)
            }
            val status = Response.Status.fromStatusCode(response.status)
            return if (statusMappers.containsKey(status)) {
                val timed = timed(statusMappers[status]!!.invoke(response), start)
                timed.withResponse(response)
                timed
            } else {
                val timed = timed(defaultMapper.invoke(response), start)
                timed.withResponse(response)
                timed
            }
        }

    private fun timed(restResult: RestResult<T>, start: Instant): RestResult<T> {
        return restResult.withTurnaroundTime(timeSince(start))
    }

    private fun timeSince(start: Instant): Duration {
        return Duration.between(start, Instant.now())
    }

    fun setRetries(retries: Int) {
        this.retries = retries
    }

    companion object {
        fun <T> withResponseSupplier(responseSupplier: Supplier<Response>): RestRequester<T> {
            val requester = RestRequester<T>()
            requester.responseSupplier = responseSupplier
            return requester
        }
    }
}
