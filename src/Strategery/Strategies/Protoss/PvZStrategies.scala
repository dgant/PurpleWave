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
object PvZReaver    extends PvZStrategy {
  blacklistVs(With.fingerprints.twoHatchMuta, With.fingerprints.threeHatchMuta)
}
object PvZSpeedlot  extends PvZStrategy

abstract class PvZFFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
}
object PvZFFE extends PvZFFEOpening {
  addSelectionRequirement(() => With.placement.wall.isDefined)
}
object PvZGatewayFE extends PvZFFEOpening {
  addSelectionRequirement(() => With.placement.wall.isDefined)
  setMinimumGamesVsOpponent(2)
  whitelistVs(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool)
  blacklistVs(With.fingerprints.fourPool, With.fingerprints.ninePool)
}