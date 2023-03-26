package Strategery.Strategies.Protoss

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.FFA.{ProtossFFA, ProtossFFAMoney}
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
