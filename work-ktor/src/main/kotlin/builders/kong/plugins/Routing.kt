package builders.kong.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.lang.System.currentTimeMillis

@Serializable
data class MyResponse(val message: String)

fun Application.configureRouting() {
    val log = KotlinLogging.logger {}

    routing {
        val meetServerUrl = environment!!.config.property("ktor.meet.server.url").getString()
        log.info { "💉 Got meeting server url from environment: $meetServerUrl" }

        get("/work") {
            var originalHeaders: Headers = call.request.headers
            val client = HttpClient(CIO)
            val start = currentTimeMillis()
            var count = 0
            for (i in (1..4)) {
                var response = client.request("$meetServerUrl/meet") {
                    method = HttpMethod.Get
                    headers {
                        append(HttpHeaders.UserAgent, "Ktor Client 🤘")
                        // propagate http headers (including tracing) from request to meeting request 
                        for (header in originalHeaders.entries()) {
                            if (HttpHeaders.Host != header.key) {
                                log.info { header.value }
                                if (header.value.size != 1) {
                                    append(header.key, header.value.joinToString { ", " })
                                } else
                                    append(header.key, header.value[0])
                            }
                        }
                    }
                }
                if (response.status.isSuccess()) {
                    log.info { "\uD83D\uDEB6 Going to meeting: $i" }
                    count++
                }
            }
            val end = currentTimeMillis()
            var message = "\uD83D\uDCC6 worked for ${end - start} ms, and went to $count meetings"
            call.respond(HttpStatusCode.OK, MyResponse(message))
            client.close()
        }
        get("/") {
            call.respond("IMOK")
        }
    }
}
