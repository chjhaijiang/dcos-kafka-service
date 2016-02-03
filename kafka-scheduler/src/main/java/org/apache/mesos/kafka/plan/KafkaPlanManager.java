package org.apache.mesos.kafka.plan;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.mesos.kafka.config.KafkaConfigService;

import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.scheduler.plan.Block;
import org.apache.mesos.scheduler.plan.Phase;
import org.apache.mesos.scheduler.plan.Plan;

public class KafkaPlanManager implements Observer {
  private final Log log = LogFactory.getLog(KafkaPlanManager.class);

  private KafkaConfigService envConfig = KafkaConfigService.getEnvConfig();

  Plan plan = null;
  PlanStrategy strategy = null;

  public KafkaPlanManager(Plan plan) {
    this.plan = plan;
    setStrategy(plan);
  }

  private void setStrategy(Plan plan) {
    String strat = envConfig.get("PLAN_STRATEGY");

    switch (strat) {
      case "INSTALL":
        strategy = new KafkaInstallStrategy(plan);
        break;
      case "STAGE":
        strategy = new KafkaStageStrategy(plan);
        break;
      default:
        strategy = new KafkaStageStrategy(plan);
        break;
    }
  }

  public List<? extends Phase> getPhases() {
    return plan.getPhases();
  }

  public void update(Observable observable, Object obj) {
    TaskStatus taskStatus = (TaskStatus) obj;
    log.info("Publishing TaskStatus update: " + taskStatus);

    Block currBlock = getCurrentBlock();

    if (currBlock != null) {
      getCurrentBlock().update(taskStatus);
    }
  }

  public Block getCurrentBlock() {
    return strategy.getCurrentBlock();
  }

  public boolean planIsComplete() {
    return getCurrentBlock() == null;
  }

  public void proceed() {
    strategy.proceed();
  }

  public void interrupt() {
    strategy.interrupt();
  }
}
