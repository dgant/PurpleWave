package Planning.Plans.Army

import Lifecycle.With
import Planning.Plan

class Aggression(aggressionRatio: Double) extends Plan {
  
  override def onUpdate() {
    With.blackboard.aggressionRatio = aggressionRatio
  }
}
