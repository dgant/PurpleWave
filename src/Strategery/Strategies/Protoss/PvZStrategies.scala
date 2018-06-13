package Strategery.Strategies.Protoss

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
}
class PvZ2GateOpening extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )
}
object PvZ2Gate99 extends PvZ2GateOpening
object PvZProxy2Gate extends PvZ2GateOpening {
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
object PvZ4GateDragoonAllIn extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZ4Gate99 extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZEarlyFFEConservative      extends PvZFFEOpening
object PvZEarlyFFEEconomic          extends PvZFFEOpening
object PvZMidgameCorsairSpeedlot    extends PvZStrategy
object PvZMidgameGatewayAttack      extends PvZStrategy
//object PvZMidgameCorsairDarkTemplar extends PvZStrategy
//object PvZMidgameCorsairReaver extends PvZStrategy


