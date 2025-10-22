package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvR.PvR2Gate4Gate
import Gameplans.Protoss.PvZ.{PvZ1GateCoreExpand, PvZ1GateCoreYoloDragoon, PvZ1GateCoreYoloSpeedlot}
import Lifecycle.With
import Planning.Plans.Plan
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Zerg)
}

object PvZ2Gate4Gate extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvR2Gate4Gate)
}
object PvZ1GateCoreExpand extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1GateCoreExpand)
}
object PvZ1GateCoreYoloSpeedlot extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1GateCoreYoloSpeedlot)
}
object PvZ1GateCoreYoloDragoon extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ1GateCoreYoloDragoon)
}

object PvZ1BaseReactive extends PvZStrategy {
  addChoice(PvZGoon,    PvZReaver,  PvZSpeedlot)
  addChoice(PvZMuscle,  PvZExpand,  PvZTech)
}
object PvZMuscle extends PvZStrategy {
  addActivationRequirement(() => unitsEver(Protoss.Gateway) >= 2)
}
object PvZExpand extends PvZStrategy {
  addActivationRequirement(() => With.geography.maxMiningBasesOurs >= 2)
}
object PvZTech extends PvZStrategy {
  addActivationRequirement(() => haveEver(Protoss.Assimilator))
}
object PvZGoon extends PvZStrategy {
  addActivationRequirement(() => haveEver(Protoss.Dragoon) && upgradeStarted(Protoss.DragoonRange))
}
object PvZReaver extends PvZStrategy {
  blacklistVs(With.fingerprints.twoHatchMuta, With.fingerprints.threeHatchMuta)
  addActivationRequirement(() => haveEver(Protoss.RoboticsFacility))
}
object PvZSpeedlot extends PvZStrategy {
  addActivationRequirement(() => haveEver(Protoss.Forge))
}


abstract class PvZFFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
}
object PvZFFE extends PvZFFEOpening {
  addSelectionRequirement(() => With.placement.wall.exists(_.buildings.count(_._2 == Protoss.PhotonCannon) >= 6))
}
object PvZGatewayFE extends PvZFFEOpening {
  addSelectionRequirement(() => With.placement.wall.isDefined)
  setMinimumGamesVsOpponent(2)
  whitelistVs(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool)
  blacklistVs(With.fingerprints.fourPool, With.fingerprints.ninePool)
}