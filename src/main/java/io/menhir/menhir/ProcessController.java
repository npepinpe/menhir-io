package io.menhir.menhir;

import io.camunda.zeebe.client.ZeebeClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/process")
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  private final ZeebeClient zeebe;

  @Autowired
  public ProcessController(ZeebeClient client) {
    this.zeebe = client;
  }

  @PostMapping("/order/{orderId}")
  public void order(@PathVariable String orderId, @RequestBody OrderRequest request) {

    LOG.info(
        "Publishing message `io.menhir.order` with correlation key `{}` and variables: {}",
        orderId,
        request);

    zeebe
        .newPublishMessageCommand()
        .messageName("io.menhir.order")
        .correlationKey(orderId)
        .variables(Map.of("orderId", orderId, "menhirRequestCount", request.menhirRequestCount()))
        .send();
  }

  record OrderRequest(int menhirRequestCount) {}
}
