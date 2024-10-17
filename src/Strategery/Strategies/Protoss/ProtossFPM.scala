package Strategery.Strategies.Protoss

import Planning.Plan
import Planning.Plans.Gameplans.Protoss.FPM.PvPFPM
import Strategery.Strategies.Strategy
import bwapi.Race

class AbstractProtossFPM extends Strategy {
  setOurRace(Race.Protoss)
  setMoneyMap(true)
}

object PvTFPM extends AbstractProtossFPM {
  setEnemyRace(Race.Terran)
}

object PvPFPM extends AbstractProtossFPM {
  override def gameplan: Option[Plan] = Some(new PvPFPM)
  setEnemyRace(Race.Protoss)
}

object PvZFPM extends AbstractProtossFPM {
  setEnemyRace(Race.Zerg)
}

object PvRFPM extends AbstractProtossFPM {
  setEnemyRace(Race.Unknown)
}