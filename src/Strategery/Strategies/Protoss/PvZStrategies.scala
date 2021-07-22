package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZ2GateNew
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
object PvZ2Gate4Gate extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ2GateNew)
}
object PvZ1BaseForgeTech extends PvZStrategy {
  override def allowedVsHuman: Boolean = false
  //Disabled for AIIDE round robin
  //override def responsesWhitelisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZSpeedlot extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZ4GateGoon extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgame5GateGoon, PvZMidgame5GateGoonReaver))
}
object PvZCorsair extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZDT extends PvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZProxy2Gate extends PvZ2GateOpening {
  override def mapsBlacklisted: Iterable[StarCraftMap] = MapGroups.badForProxying
  override def responsesWhitelisted = Iterable(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)
}
object PvZ10Gate extends PvZ2GateOpening {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvZSpeedlot, PvZ4GateGoon, PvZDT, PvZCorsair))
  override def responsesBlacklisted = Iterable(With.fingerprints.fourPool)
  override def minimumGamesVsOpponent: Int = 1
}
object PvZ2Gate1012 extends PvZ2GateOpening {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvZSpeedlot, PvZ4GateGoon, PvZDT))
}
object PvZ2Gate910 extends PvZ2GateOpening {
  override def choices: Iterable[Iterable[Strategy]] = Iterable(Iterable(PvZSpeedlot, PvZ4GateGoon, PvZDT))
  override def responsesWhitelisted: Iterable[Fingerprint] = Vector(With.fingerprints.fourPool, With.fingerprints.twelveHatch)
}
object PvZFFEConservative extends PvZFFEOpening {
  override def responsesWhitelisted: Iterable[Fingerprint] = Vector(With.fingerprints.fourPool)
  override def allowedVsHuman: Boolean = false
}
object PvZFFEEconomic     extends PvZFFEOpening
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
