package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZ2GateFlex
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

abstract class PvZFFEOpening extends PvZStrategy {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForWalling ++ MapGroups.tooShortForFFE
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzMidgameTransitioningFromTwoBases
  )
}
abstract class PvZ2GateOpening extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    ProtossChoices.pvzOpenersTransitioningFrom2Gate
  )
}
object PvZ2GateFlex extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ2GateFlex)
}
object PvZ1BaseForgeTech extends PvZStrategy {
  override def allowedVsHuman: Boolean = false
  //Disabled for AIIDE round robin
  //override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZProxy2Gate extends PvZ2GateOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesWhitelisted = Iterable(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)
}
object PvZFFE     extends PvZFFEOpening
object PvZGatewayFE       extends PvZFFEOpening {
  override def minimumGamesVsOpponent: Int = 2
  override def responsesWhitelisted = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch)
  override def responsesBlacklisted = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool, With.fingerprints.twelvePool)
}

object PvZLateGameTemplar extends PvZStrategy
object PvZLateGameReaver  extends PvZStrategy

object PvZMidgame5GateGoon            extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgame5GateGoonReaver      extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver))  }
object PvZMidgameCorsairReaverZealot  extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver)) }
object PvZMidgameCorsairReaverGoon    extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameReaver)) }
object PvZMidgameBisu                 extends PvZStrategy { override def choices: Iterable[Iterable[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
