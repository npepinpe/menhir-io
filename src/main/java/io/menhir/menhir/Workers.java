package io.menhir.menhir;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
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

    LOG.info(
        "Computing appropriate number of parallel chiseling requests based on quality of rock for order {} ",
        order);

    final var multiplier = RandomUtils.nextInt(5, 50);
    final var count = order.blocksOfRock() * multiplier;
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
    final var sculptedCount = RandomUtils.nextInt(1, 5);
    final var menhirs = new ArrayList<>();
    LOG.debug("Sculpting {} menhir out of block {}", sculptedCount, request);
    for (int i = 0; i < sculptedCount; i++) {
      menhirs.add(new Menhir(RandomStringUtils.randomAlphanumeric(6)));
    }

    client.newCompleteCommand(job).variables(Map.of("menhir", menhirs)).send().join();
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
