package Strategery.Strategies.Protoss

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Personalities.PersonalityCheese
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Zerg)
}

object PvZ2Gate4Gate extends PvZStrategy
object PvZ1GateCoreYoloSpeedlot extends PvZStrategy
object PvZ1GateCoreYoloDragoon extends PvZStrategy
object PvZ1GateCoreExpand extends PvZStrategy {
  banPersonality(PersonalityCheese)
}

object PvZ1BaseReactive extends PvZStrategy {
  addChoice(PvZGoon,    PvZReaver,  PvZSpeedlot)
  addChoice(PvZMuscle,  PvZExpand,  PvZTech)
  banPersonality(PersonalityCheese)
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
abstract class PvZFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
  addSelectionRequirement(() => With.placement.wall.exists(_.buildings.count(_._2 == Protoss.PhotonCannon) >= 6))
}

object PvZFFE extends PvZFEOpening {
  banPersonality(PersonalityCheese)
}
object PvZGFE extends PvZFEOpening {
  setMinimumGamesVsOpponent(2)
  banPersonality(PersonalityCheese)
  whitelistVs(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.overpool)
  blacklistVs(With.fingerprints.fourPool, With.fingerprints.ninePool)
}
