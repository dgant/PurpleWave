package Strategery.Strategies.Protoss

import Gameplans.Protoss.PvR.PvRGFEComplete
import Planning.Plans.Plan
import Strategery.Strategies.Personalities.PersonalityCheese
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Seq[Race] = Vector(Race.Unknown)
}

object PvR2Gate4Gate extends PvRStrategy {
  addChoiceVs(Race.Terran)(PvT4Gate)
  addChoiceVs(Race.Protoss)(PvP4GateGoon)
  addChoiceVs(Race.Zerg)(PvZ2Gate4Gate)
}
object PvRDT extends PvRStrategy {
  addChoiceVs(Race.Terran)(PvTDT)
  addChoiceVs(Race.Protoss)(PvPDT)
  addChoiceVs(Race.Zerg)(PvZ1BaseReactive, PvZ1GateCoreYoloSpeedlot)
}
object PvR1012 extends PvRStrategy {
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_2Gate: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_2Gate: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_2Gate: _*)
}
object PvRZZCoreZ extends PvRStrategy {
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_GateCore: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_GateCore: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_GateCore: _*)
}
object PvRFFE extends PvRStrategy {
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_FFE: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_FFE: _*)
  banPersonality(PersonalityCheese)
}
object PvRGFE extends PvRStrategy {
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_GFE: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_GFE: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_GFE: _*)
  banPersonality(PersonalityCheese)
}

object PvRGFEComplete extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvRGFEComplete())
  banPersonality(PersonalityCheese)
}


object PvRReaver extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new Gameplans.Protoss.PvR.PvRReaver)
}