package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass

class FloatBuildings(val toFloat: UnitClass*) extends Plan {
  override def onUpdate() {
    With.blackboard.floatableBuildings.set(With.blackboard.floatableBuildings() ++ toFloat)
  }
}