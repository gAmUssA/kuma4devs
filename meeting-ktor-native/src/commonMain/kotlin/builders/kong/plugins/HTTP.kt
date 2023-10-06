package builders.kong.plugins

import io.ktor.server.application.*
// this is fork of https://github.com/ktorio/ktor/tree/main/ktor-server/ktor-server-plugins/ktor-server-default-headers
// that uses kotlinx-datetime
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor Native 🤘")
    }
}
