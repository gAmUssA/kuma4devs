package sh.kongme.work;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.micronaut.http.HttpHeaders.ACCEPT_CHARSET;
import static io.micronaut.http.HttpHeaders.ACCEPT_ENCODING;
import static io.micronaut.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static io.micronaut.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static io.micronaut.http.HttpHeaders.CONNECTION;
import static io.micronaut.http.HttpHeaders.CONTENT_LENGTH;
import static io.micronaut.http.HttpHeaders.COOKIE;
import static io.micronaut.http.HttpHeaders.DATE;
import static io.micronaut.http.HttpHeaders.EXPECT;
import static io.micronaut.http.HttpHeaders.HOST;
import static io.micronaut.http.HttpHeaders.ORIGIN;
import static io.micronaut.http.HttpHeaders.REFERER;
import static io.micronaut.http.HttpHeaders.TE;
import static io.micronaut.http.HttpHeaders.TRAILER;
import static io.micronaut.http.HttpHeaders.TRANSFER_ENCODING;
import static io.micronaut.http.HttpHeaders.UPGRADE;
import static io.micronaut.http.HttpHeaders.USER_AGENT;
import static io.micronaut.http.HttpHeaders.VIA;
import static io.micronaut.http.HttpResponse.ok;
import static io.micronaut.http.MediaType.TEXT_PLAIN;
import static java.lang.System.currentTimeMillis;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

@Controller
@Slf4j
public class RestController {

  @Value(("${meet.url:`http://localhost:8080`}"))
  String url;

  @SneakyThrows
  @Get(uri = "/work", produces = TEXT_PLAIN)
  public HttpResponse<String> work(HttpHeaders headers) {
    log.info("💉 Got meeting server url from environment: {}", url);
    long start = currentTimeMillis();
    int count = 0;

    var httpClient = HttpClient.newHttpClient();
    var builder = newBuilder(URI.create(url + "/meet")).GET()
        .version(HttpClient.Version.HTTP_1_1)
        // propagate/forward request headers, e.g., for tracing headers 
        .headers(ofRequest(headers).toArray(new String[0]))
        .setHeader(USER_AGENT, "Java/9")
        .setHeader(VIA, "Micronaut");
    var request = builder.build();

    for (int i = 0; i < 4; i++) {
      var response = httpClient.send(request, discarding());
      if (response.statusCode() == 200) {
        log.info("\uD83D\uDEB6 Going to meeting: {}", i);
        count++;
      }
    }
    long end = currentTimeMillis();
    return ok("\uD83D\uDCC6 worked for " + (end - start) + "ms, and went to " + count + " meetings");
  }

  @Get(value = "/ruok", produces = TEXT_PLAIN)
  public HttpResponse<String> healthcheck() {
    return ok("imok");
  }

  private List<String> ofRequest(final HttpHeaders headers) {
    return stream(headers.spliterator(), false)
        .filter(entry -> FORBIDDEN_HEADERS.stream().noneMatch(s -> s.equalsIgnoreCase(entry.getKey())))
        .map(entry -> new String[]{entry.getKey(), entry.getValue().stream().map(Objects::toString).collect(joining(", "))})
        .flatMap(strings -> Stream.of(strings[0], strings[1]))
        .collect(Collectors.toList());
  }

  // https://developer.mozilla.org/en-US/docs/Glossary/Forbidden_header_name
  public static List<String> FORBIDDEN_HEADERS = List.of(
      ACCEPT_CHARSET,
      ACCEPT_ENCODING,
      ACCESS_CONTROL_REQUEST_HEADERS,
      ACCESS_CONTROL_REQUEST_METHOD,
      CONNECTION, CONTENT_LENGTH, COOKIE,
      DATE, EXPECT, HOST, ORIGIN, REFERER, TE,
      TRAILER, TRANSFER_ENCODING, UPGRADE, VIA
  );
}
