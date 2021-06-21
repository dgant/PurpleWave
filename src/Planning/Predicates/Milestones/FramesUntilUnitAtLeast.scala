package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import ProxyBwapi.UnitClasses.UnitClass

case class FramesUntilUnitAtLeast(unitClass: UnitClass, frames: () => Int) extends Predicate {

  def this(unitClass: UnitClass, specificFrames: Int) = this(unitClass, () => specificFrames)

  override def apply: Boolean = MacroFacts.framesUntilUnit(unitClass) >= frames()
}
