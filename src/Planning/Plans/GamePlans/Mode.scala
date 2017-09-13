package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.Parallel

abstract class Mode extends Parallel {
  
  description.set("Strategy mode")
  
  override def isComplete: Boolean = completionCriteria.isComplete || ! activationCriteria.isComplete
  
  val completionCriteria: Plan
  val activationCriteria: Plan
  
  override def onUpdate() {
    if ( ! isComplete) {
      super.onUpdate()
    }
  }
}
