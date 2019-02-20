package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.UnitClasses.UnitClass

class FramesUntilUnitAtLeast(unitClass: UnitClass, frames: () => Int) extends Predicate {

  def this(unitClass: UnitClass, specificFrames: Int) = this(unitClass, () => specificFrames)

  override def isComplete: Boolean = With.projections.unit(unitClass) >= frames()
}
