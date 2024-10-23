package Strategery.Strategies.Protoss

import Gameplans.Protoss.FFA.{ProtossFFA, ProtossFFAMoney}
import Planning.Plan
import Strategery.Hunters
import Strategery.Strategies.Strategy
import bwapi.Race

class AbstractProtossFFA extends Strategy {
  override def gameplan: Option[Plan] = { Some(new ProtossFFA) }
  setOurRace(Race.Protoss)
  setFFA(true)
}

object ProtossFFA extends AbstractProtossFFA {
  blacklistOn(Hunters)
}

object ProtossFFAHunters extends AbstractProtossFFA {
  override def gameplan: Option[Plan] = { Some(new ProtossFFAMoney) }
  whitelistOn(Hunters)
  setMoneyMap(false) // BGH will be handled by ProtossFFAMoney, below
}

object ProtossFFAMoney extends AbstractProtossFFA {
  override def gameplan: Option[Plan] = { Some(new ProtossFFAMoney) }
  setMoneyMap(true)
}
