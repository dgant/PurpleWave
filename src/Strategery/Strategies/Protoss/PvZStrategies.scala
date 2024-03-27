package Strategery.Strategies.Protoss

import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Zerg)
}

object PvZ1BaseReactive extends PvZStrategy {
  addChoice(PvZGoon,    PvZReaver,  PvZSpeedlot)
  addChoice(PvZMuscle,  PvZExpand,  PvZTech)
}

object PvZMuscle    extends PvZStrategy
object PvZExpand    extends PvZStrategy
object PvZTech      extends PvZStrategy
object PvZGoon      extends PvZStrategy
object PvZReaver    extends PvZStrategy
object PvZSpeedlot  extends PvZStrategy

abstract class PvZFFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
}
object PvZFFE extends PvZFFEOpening {
  override def addRequirement(predicate: () => Boolean): Unit = With.placement.wall.isDefined
}
object PvZGatewayFE extends PvZFFEOpening {
  override def addRequirement(predicate: () => Boolean): Unit = With.placement.wall.isDefined
  override def minimumGamesVsOpponent: Int = 2
  override def responsesWhitelisted = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool)
  override def responsesBlacklisted = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool)
}