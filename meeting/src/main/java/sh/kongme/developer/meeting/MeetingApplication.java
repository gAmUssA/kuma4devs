package sh.kongme.developer.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.*;

@SpringBootApplication
@RestController
@Slf4j
public class MeetingApplication {

  @GetMapping("/meet")
  public void meeting(@RequestHeader Map<String, String> headers) {

    log.info("Client Headers: {}", headers);

    log.info("\uD83D\uDC68\u200D\uD83C\uDFED gone meeting...");
    try {
      sleep(250L);
    } catch (InterruptedException e) {
      // ¯\_( ͡° ͜ʖ ͡°)_/¯
    }
  }


  public static void main(String[] args) {
    SpringApplication.run(MeetingApplication.class, args);
  }

}
