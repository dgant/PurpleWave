package Strategery.Strategies.Protoss

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Seq[Race] = Vector(Race.Unknown)
}

object PvR2Gate4Gate extends PvRStrategy {
  addChoice(PvT4Gate)
  addChoice(PvP4GateGoon)
  addChoice(PvZ2Gate4Gate)
}
object PvRDT extends PvRStrategy {
  addChoice(PvTDT)
  addChoice(PvPDT)
  addChoice(PvZ1BaseReactive, PvZ1GateCoreYoloSpeedlot)
}
object PvR2Gate1012 extends PvRStrategy {
  addChoice(ProtossChoices.vsTerran_From_2Gate: _*)
  addChoice(ProtossChoices.vsProtoss_From_2Gate: _*)
  addChoice(ProtossChoices.vsZerg_From_2Gate: _*)
}
object PvRZZCoreZ extends PvRStrategy {
  addChoice(ProtossChoices.vsTerran_From_GateCore: _*)
  addChoice(ProtossChoices.vsProtoss_From_GateCore: _*)
  addChoice(ProtossChoices.vsZerg_From_GateCore: _*)
}
object PvRFFE extends PvRStrategy {
  addChoice(ProtossChoices.vsTerran_From_FFE: _*)
  addChoice(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoice(ProtossChoices.vsZerg_From_FFE: _*)
}
object PvRGFE extends PvRStrategy {
  addChoice(ProtossChoices.vsTerran_From_GFE: _*)
  addChoice(ProtossChoices.vsProtoss_From_GFE: _*)
  addChoice(ProtossChoices.vsZerg_From_GFE: _*)
}

