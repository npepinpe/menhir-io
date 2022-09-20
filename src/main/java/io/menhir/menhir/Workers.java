package io.menhir.menhir;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Workers {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger LOG = LoggerFactory.getLogger(Workers.class);

  public Workers() {
    int a = 1;
  }

  @ZeebeWorker(
      type = "io.menhir.compute",
      maxJobsActive = 30,
      fetchVariables = {"orderId", "blocksOfRock"})
  public void compute(final JobClient client, final ActivatedJob job) {
    final var order = job.getVariablesAsType(MenhirOrder.class);

    LOG.info("Computing appropriate number of parallel chiseling requests out of order {} ", order);

    final var count = order.blocksOfRock() * 100;
    final List<MenhirRequest> requests = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      requests.add(new MenhirRequest(RandomStringUtils.randomAlphabetic(6)));
    }

    client.newCompleteCommand(job).variables(Map.of("menhirRequests", requests)).send().join();
  }

  @ZeebeWorker(
      type = "io.menhir.sculpt",
      maxJobsActive = 30,
      fetchVariables = {"menhirRequest"})
  public void sculpt(final JobClient client, final ActivatedJob job) {
    final var request = getVariableAs(job, "menhirRequest", MenhirRequest.class);
    LOG.debug("Sculpting menhir {}", request);
    client
        .newCompleteCommand(job)
        .variables(Map.of("menhir", new Menhir(request.id())))
        .send()
        .join();
  }

  @ZeebeWorker(
      type = "io.menhir.deliver",
      maxJobsActive = 30,
      fetchVariables = {"menhir"})
  public void deliver(final JobClient client, final ActivatedJob job) {
    final var request = getVariableAs(job, "menhir", Menhir.class);
    LOG.debug("Delivering menhir {}", request);
    final var receipt = new MenhirReceipt(request.id(), RandomStringUtils.randomAlphanumeric(6));
    client.newCompleteCommand(job).variables(Map.of("menhirReceipt", receipt)).send().join();
  }

  private <T> T getVariableAs(final ActivatedJob job, final String variable, final Class<T> clazz) {
    return MAPPER.convertValue(job.getVariablesAsMap().get(variable), clazz);
  }

  record MenhirOrder(String orderId, int blocksOfRock) {}

  record MenhirRequest(String id) {}

  record Menhir(String id) {}

  record MenhirReceipt(String menhirId, String receiptId) {}
}
