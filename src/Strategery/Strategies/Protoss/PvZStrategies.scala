package Strategery.Strategies.Protoss

import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.PvZ.{PvZ1Base4GateGoon, PvZ1BaseGoonReaver, PvZ1BaseSpeedlotArchon, PvZ1BaseStargate, PvZ2022}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Zerg)
}

object PvZ2022 extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ2022)
}

object PvZ1Base4GateGoon extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1Base4GateGoon)
}

object PvZ1BaseGoonReaver extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1BaseGoonReaver)
}

object PvZ1BaseSpeedlotArchon extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1BaseSpeedlotArchon)
}

object PvZ1BaseStargate extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1BaseStargate)
}

abstract class PvZFFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
}
object PvZFFE extends PvZFFEOpening
object PvZGatewayFE extends PvZFFEOpening {
  override def minimumGamesVsOpponent: Int = 2
  override def responsesWhitelisted = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool)
  override def responsesBlacklisted = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.twelvePool)
}