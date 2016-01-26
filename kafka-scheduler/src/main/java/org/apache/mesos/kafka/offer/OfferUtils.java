package org.apache.mesos.kafka.offer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.mesos.config.ConfigurationService;
import org.apache.mesos.kafka.config.KafkaConfigService;
import org.apache.mesos.kafka.state.KafkaStateService;
import org.apache.mesos.offer.OfferRequirement;

import org.apache.mesos.Protos.TaskInfo;

public class OfferUtils {
  private static final Log log = LogFactory.getLog(OfferUtils.class);
  private static ConfigurationService config = KafkaConfigService.getConfigService();
  private static KafkaStateService state = KafkaStateService.getStateService();

  public static Integer getNextBrokerId() {
    try {
      List<String> taskNames = state.getTaskNames();

      int brokerId = 0;

      while (taskNames.contains(getBrokerName(brokerId))) {
        brokerId++;
      }

      return brokerId;
    } catch (Exception ex) {
      log.error("Failed to get task names with exception: " + ex);
      return null;
    }
  }

  public static String getBrokerName(int brokerId) {
      return "broker-" + brokerId;
  }

  public static boolean belowTargetBrokerCount() {
    int targetBrokerCount = Integer.parseInt(config.get("BROKER_COUNT"));
    int currentBrokerCount = Integer.MAX_VALUE;

    try {
      currentBrokerCount = state.getTaskNames().size();
    } catch(Exception ex) {
      log.error("Failed to retrieve current broker count with exception: " + ex);
    }

    return currentBrokerCount < targetBrokerCount;
  }
}