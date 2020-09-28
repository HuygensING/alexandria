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

import nl.knaw.huygens.alexandria.markup.api.ErrorEntity
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.time.Duration
import java.util.*
import javax.ws.rs.core.Response

class RestResult<T> {
    private var failure = false
    private var cargo: T? = null
    private var response: Response? = null
    private var exception: Exception? = null
    private var errorMessage: String? = null

    var turnaroundTime: Duration? = null
        private set

    fun withCargo(cargo: T): RestResult<T> {
        this.cargo = cargo
        return this
    }

    fun get(): T {
        return cargo!!
    }

    fun withFail(failure: Boolean) {
        this.failure = failure
    }

    fun hasFailed(): Boolean {
        return failure
    }

    fun withResponse(response: Response) {
        this.response = response
    }

    fun getResponse(): Optional<Response> {
        return Optional.ofNullable(response)
    }

    fun withException(exception: Exception) {
        this.exception = exception
    }

    fun getException(): Optional<Exception> =
            Optional.ofNullable(exception)

    private fun withErrorMessage(errorMessage: String) {
        this.errorMessage = errorMessage
    }

    fun getErrorMessage(): Optional<String> =
            Optional.ofNullable(errorMessage)

    val failureCause: Optional<String>
        get() {
            val cause: String? = when {
                errorMessage != null -> errorMessage
                exception != null -> exception!!.message
                response != null ->
                    "Unexpected return status: ${response!!.status} ${response!!.statusInfo}"
                else -> null
            }
            return Optional.ofNullable(cause)
        }

    fun withTurnaroundTime(processingTime: Duration?): RestResult<T> {
        turnaroundTime = processingTime
        return this
    }

    override fun toString(): String =
            ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE)

    companion object {
        fun <T> failingResult(response: Response): RestResult<T> {
            val result = RestResult<T>().apply {
                withFail(true)
                withResponse(response)
            }
            if (response.hasEntity()) {
                try {
                    val errorEntity = response.readEntity(ErrorEntity::class.java)
                    result.withErrorMessage(errorEntity.message!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return result
        }

        fun <T> failingResult(exception: Exception): RestResult<T> =
                RestResult<T>().apply {
                    withFail(true)
                    withException(exception)
                }

        fun <T> failingResult(errorMessage: String): RestResult<T> =
                RestResult<T>().apply {
                    withFail(true)
                    withErrorMessage(errorMessage)
                }
    }
}
