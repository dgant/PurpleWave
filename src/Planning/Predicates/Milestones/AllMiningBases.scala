package Planning.Predicates.Milestones

import Information.Geography.Types.Base
import Lifecycle.With

object AllMiningBases {
  
  def apply(): Iterable[Base] = {
    With.geography.ourBases
      .filter(base =>
        base.townHall.isDefined &&
        base.minerals.size >= 5 &&
        base.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut)
  }

  def isMiningBase(base: Base): Boolean = base.minerals.size >= 5 && base.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut
}
