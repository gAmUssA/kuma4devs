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

  private List<String> ofRequest(final HttpHeaders headers) {
    final List<String> collect = stream(headers.spliterator(), false)
        .filter(entry -> {
          if (entry.getKey().equalsIgnoreCase(HttpHeaders.HOST)) {
            return false;
          }
          return true;
        })
        .map(entry -> new String[]{entry.getKey(), entry.getValue().stream().map(Objects::toString).collect(joining(", "))})
        .flatMap(strings -> Stream.of(strings[0], strings[1]))
        .collect(Collectors.toList());
    //System.out.println(collect);
    return collect;
  }
}
