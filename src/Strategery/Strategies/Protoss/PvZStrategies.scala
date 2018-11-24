package Strategery.Strategies.Protoss

import Lifecycle.With
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

class PvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

class PvZFFEOpening extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )

  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForWalling
}
class PvZ2GateOpening extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )
}
object PvZ2Gate99 extends PvZ2GateOpening
object PvZProxy2Gate extends PvZ2GateOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
}
object PvZ4GateDragoonAllIn extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZ4Gate99 extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZEarlyFFEConservative      extends PvZFFEOpening
object PvZEarlyFFEEconomic          extends PvZFFEOpening
object PvZEarlyFFEGreedy            extends PvZFFEOpening {
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool, With.fingerprints.twelvePool)
}
object PvZMidgame4Gate2Archon       extends PvZStrategy
object PvZMidgameBisu               extends PvZStrategy
object PvZMidgameNeoBisu    extends PvZStrategy
object PvZ5GateGoon      extends PvZStrategy
