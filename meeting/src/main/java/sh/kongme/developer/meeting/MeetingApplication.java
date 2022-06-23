package sh.kongme.developer.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RestController
@Slf4j
public class MeetingApplication {

  @SneakyThrows
  @GetMapping("/meet")
  public void meeting() {
    log.info("(◔_◔) gone meeting...");
    Thread.sleep(250L);
  }


  public static void main(String[] args) {
    SpringApplication.run(MeetingApplication.class, args);
  }

}
