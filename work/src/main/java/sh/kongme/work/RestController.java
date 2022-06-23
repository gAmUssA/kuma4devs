package sh.kongme.work;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class RestController {

  @Client(("${meet.url:`http://localhost:8080`}"))
  @Inject
  HttpClient httpClient;

  @Get(uri = "/work", produces = MediaType.TEXT_PLAIN)
  public String work() {
    long start = System.currentTimeMillis();
    int count = 0;
    for (int i = 0; i < 4; i++) {
      log.info("\uD83D\uDEB6 Going to meeting: {}", i);

      String uri = UriBuilder.of("/meet").toString();

      final HttpResponse<String> httpResponse = httpClient.toBlocking().exchange(uri);

      if (httpResponse.status() == HttpStatus.OK) {
        count++;
      }
    }
    long end = System.currentTimeMillis();
    return "\uD83D\uDCC6 worked for " + (end - start) + "ms, and went to " + count + " meetings";
  }

}
