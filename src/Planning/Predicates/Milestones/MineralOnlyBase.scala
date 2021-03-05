package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class MineralOnlyBase extends Predicate {
  override def apply: Boolean = With.geography.ourBases.exists(base =>
    base.gas.isEmpty
    && base.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut)
}
