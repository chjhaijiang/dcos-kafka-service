package org.apache.mesos.kafka.plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.mesos.kafka.config.KafkaConfigService;
import org.apache.mesos.kafka.offer.OfferRequirementProvider;
import org.apache.mesos.kafka.scheduler.KafkaScheduler;
import org.apache.mesos.scheduler.plan.Block;
import org.apache.mesos.scheduler.plan.Phase;

public class KafkaPhase implements Phase {
  private List<Block> blocks = null;
  private String configName = null;

  public KafkaPhase(
      String configName,
      OfferRequirementProvider offerReqProvider) {

    this.configName = configName;
    this.blocks = createBlocks(configName, offerReqProvider);
  }

  public List<Block> getBlocks() {
    return blocks;
  }

  private List<Block> createBlocks(
      String configName,
      OfferRequirementProvider offerReqProvider) {

    List<Block> blocks = new ArrayList<Block>();
    KafkaConfigService config = KafkaScheduler.getConfigState().fetch(configName); 

    for (int i=0; i<config.getBrokerCount(); i++) {
      blocks.add(new KafkaBlock(offerReqProvider, configName, i));
    }

    return blocks;
  }

  public Block getCurrentBlock() {
    if (blocks.size() == 0) {
      return null;
    }

    for (Block block : blocks) {
      if (!block.isComplete()) {
        return block;
      }
    }
    
    return null;
  }

  public boolean isComplete() {
    for (Block block : blocks) {
      if (!block.isComplete()) {
        return false;
      }
    }

    return true;
  }

  public String getName() {
    return "Update to " + configName;
  }

  public int getId() {
    return 0;
  }
}

