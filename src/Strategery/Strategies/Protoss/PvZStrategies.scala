package Strategery.Strategies.Protoss

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.Protoss.PvZ.PvZ2022
import Strategery.Strategies.Strategy
import Strategery.{MapGroups, StarCraftMap}
import bwapi.Race

abstract class PvZStrategy extends Strategy {
  setOurRace(Race.Protoss)
  setEnemyRace(Race.Zerg)
}

abstract class PvZFFEOpening extends PvZStrategy {
  setRushTilesMinimum(160)
  addChoice(PvZMidgame5GateGoon, PvZMidgame5GateGoonReaver, PvZMidgameCorsairReaverGoon, PvZMidgameBisu)
}

object PvZ2022 extends PvZStrategy {
  override def gameplan: Option[Plan] = Some(new PvZ2022)
  //addChoice(PvZ910, PvZ1012, PvZZZCoreZ, PvZGateNexus, PvZCruddyFFE)
}
object PvZ910 extends PvZStrategy {
  addChoice(PvZ1BaseCorsair, PvZ4GateGoon, PvZFE)
}
object PvZ1012 extends PvZStrategy {
  addChoice(PvZ1BaseCorsair, PvZ4GateGoon, PvZFE)
  blacklistVs(With.fingerprints.fourPool)
}
object PvZZZCoreZ extends PvZStrategy {
  addChoice(PvZ1BaseCorsair, PvZ4GateGoon)
  blacklistVs(With.fingerprints.fourPool, With.fingerprints.ninePool)
}
object PvZGateNexus extends PvZStrategy {
  addChoice(PvZFE)
  whitelistVs(With.fingerprints.tenHatch, With.fingerprints.twelveHatch)
  blacklistVs(With.fingerprints.fourPool, With.fingerprints.ninePool, With.fingerprints.overpool)
}
object PvZCruddyFFE extends PvZStrategy {
  addChoice(PvZFE)
  whitelistVs(With.fingerprints.overpool, With.fingerprints.twelvePool, With.fingerprints.tenHatch, With.fingerprints.twelveHatch)
  blacklistVs(With.fingerprints.ninePoolGas, With.fingerprints.overpoolGas, With.fingerprints.oneHatchGas, With.fingerprints.twoHatchMain)
}
object PvZ1BaseCorsair extends PvZStrategy
object PvZ4GateGoon extends PvZStrategy
object PvZFE extends PvZStrategy {
  blacklistVs(With.fingerprints.ninePoolGas, With.fingerprints.overpoolGas, With.fingerprints.oneHatchGas, With.fingerprints.twoHatchMain)
}


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
