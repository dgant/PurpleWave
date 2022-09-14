package Strategery.Strategies.Zerg

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvZ.{ZvZ10HatchLing, ZvZ12Pool, ZvZ9PoolSpeed}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class ZvZStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Seq(Race.Zerg)
  override def enemyRaces: Seq[Race] = Seq(Race.Zerg)
}

object ZvZ9PoolSpeed extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ9PoolSpeed)
}

object ZvZ12Pool extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ12Pool)
}

object ZvZ10HatchLing extends ZvZStrategy {
  override def gameplan: Option[Plan] = Some(new ZvZ10HatchLing)
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool)
}