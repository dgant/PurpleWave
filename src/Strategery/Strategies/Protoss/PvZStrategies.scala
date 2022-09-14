package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.PvZ.PvZ2Gate
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces: Seq[Race] = Vector(Race.Zerg)
}

abstract class PvZFFEOpening extends PvZStrategy {
  override def rushTilesMinimum: Int = 160
  override def choices: Seq[Seq[Strategy]] = Vector(ProtossChoices.pvzMidgameTransitioningFromTwoBases)
}

object PvZ1Base extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ2Gate)
  override def choices: Seq[Seq[Strategy]] = Seq(
    Seq(PvZ910, PvZ1012, PvZZZCoreZ),
    Seq(PvZ1BaseCorsair, PvZ4GateGoon))
}
object PvZ910 extends PvZStrategy
object PvZ1012 extends PvZStrategy {
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool)
}
object PvZZZCoreZ extends PvZStrategy {
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)
}
object PvZ1BaseCorsair extends PvZStrategy
object PvZ4GateGoon extends PvZStrategy

object PvZ1BaseForgeTech extends PvZStrategy {
  override def allowedVsHuman: Boolean = false
  //Disabled for AIIDE round robin
  //override def responsesWhitelisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool)
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def choices: Seq[Seq[Strategy]] = Vector(Seq(PvZMidgameBisu))
}
object PvZProxy2Gate extends PvZStrategy {
  override def choices: Seq[Seq[Strategy]] = Vector(
    ProtossChoices.pvzOpenersTransitioningFrom2Gate
  )
  override def mapsBlacklisted: Seq[StarCraftMap] = MapGroups.badForProxying
  override def responsesWhitelisted = Seq(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.overpool)
  override def responsesBlacklisted = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.tenHatch)
}
object PvZFFE extends PvZFFEOpening
object PvZGatewayFE extends PvZFFEOpening {
  override def minimumGamesVsOpponent: Int = 2
  override def responsesWhitelisted = Seq(With.fingerprints.twelveHatch, With.fingerprints.tenHatch)
  override def responsesBlacklisted = Seq(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool, With.fingerprints.twelvePool)
}

object PvZLateGameTemplar extends PvZStrategy
object PvZLateGameReaver  extends PvZStrategy

object PvZMidgame5GateGoon            extends PvZStrategy { override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
object PvZMidgame5GateGoonReaver      extends PvZStrategy { override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvZLateGameReaver))  }
object PvZMidgameCorsairReaverZealot  extends PvZStrategy { override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvZLateGameReaver)) }
object PvZMidgameCorsairReaverGoon    extends PvZStrategy { override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvZLateGameReaver)) }
object PvZMidgameBisu                 extends PvZStrategy { override def choices: Seq[Seq[Strategy]] = Seq(Seq(PvZLateGameTemplar)) }
