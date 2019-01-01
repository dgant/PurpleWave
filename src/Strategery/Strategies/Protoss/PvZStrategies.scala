package Strategery.Strategies.Protoss

import Lifecycle.With
import Strategery.{MapGroups, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

abstract class PvZMidgame extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )
}

abstract class PvZFFEOpening extends PvZStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForWalling
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )
}
abstract class PvZ2GateOpening extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzOpenersTransitioningFrom2Gate
  )
}
object PvZProxy2Gate extends PvZ2GateOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)
}
object PvZ4Gate1012 extends PvZ2GateOpening {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZ4Gate99 extends PvZ2GateOpening {
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromOneBase)
}
object PvZFFEEconomic          extends PvZFFEOpening
object PvZGatewayFE            extends PvZFFEOpening {
  override def minimumGamesVsOpponent: Int = 1
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool, With.fingerprints.twelvePool)
}

object PvZLateGameTemplar extends PvZStrategy
object PvZLateGameReaver  extends PvZStrategy
object PvZLateGameCarrier extends PvZStrategy

object PvZMidgame4Gate2Archon         extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgame5GateGoon            extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgame5GateGoonReaver      extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver))  }
object PvZMidgameCorsairReaverZealot  extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver, PvZLateGameCarrier)) }
object PvZMidgameCorsairReaverGoon    extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver, PvZLateGameCarrier)) }
object PvZMidgameBisu                 extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgameNeoBisu              extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgameNeoNeoBisu           extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
