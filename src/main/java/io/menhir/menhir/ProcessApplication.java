package io.menhir.menhir;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath*:/models/*.*")
@ComponentScan(basePackageClasses = ProcessApplication.class)
public class ProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }
}
