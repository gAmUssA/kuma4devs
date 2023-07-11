package sh.kongme.developer.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@SpringBootApplication
@RestController
@Slf4j
public class MeetingApplication {

  @GetMapping("/meet")
  public ResponseEntity<String> meeting(@RequestHeader Map<String, String> headers) {

    log.info("Client Headers: {}", headers);

    log.info("\uD83D\uDC68\u200D\uD83C\uDFED gone meeting...");
    
    try {
      sleep(250L);
      return new ResponseEntity<>("✅ done...", HttpStatus.OK);
    } catch (InterruptedException e) {
      // ¯\_( ͡° ͜ʖ ͡°)_/¯
    }
    return null;
  }


  public static void main(String[] args) {
    SpringApplication.run(MeetingApplication.class, args);
  }

}
