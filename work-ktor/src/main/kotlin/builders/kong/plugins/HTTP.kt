package builders.kong.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {

    install(DefaultHeaders) {
        header("X-Engine", "KtorJVM") // will send this header with each response
    }
    install(ContentNegotiation) {
        json()
    }
}
