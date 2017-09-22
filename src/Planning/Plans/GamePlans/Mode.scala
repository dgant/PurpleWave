package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.{Always, Never}

abstract class Mode extends Parallel {
  
  description.set("Strategy mode")
  
  override def isComplete: Boolean = completionCriteria.isComplete || ! activationCriteria.isComplete
  
  val completionCriteria: Plan = new Never
  val activationCriteria: Plan = new Always
  
  override def onUpdate() {
    if ( ! isComplete) {
      super.onUpdate()
    }
  }
}
