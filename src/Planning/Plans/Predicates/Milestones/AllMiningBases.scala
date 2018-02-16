package Planning.Plans.Predicates.Milestones

import Information.Geography.Types.Base
import Lifecycle.With

object AllMiningBases {
  
  def apply(): Iterable[Base] = {
    With.geography.ourBases
      .filter(base =>
        base.townHall.isDefined &&
        base.minerals.size >= 5 &&
        base.mineralsLeft > With.configuration.maxMineralsBeforeMinedOut)
  }
}
