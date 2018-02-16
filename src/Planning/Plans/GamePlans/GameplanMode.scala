package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.Parallel
import Planning.Plans.Predicates.{Always, Never}

abstract class GameplanMode extends Parallel {
  
  override def isComplete: Boolean = completionCriteria.isComplete || ! activationCriteria.isComplete
  
  val activationCriteria: Plan = new Always
  val completionCriteria: Plan = new Never
  
  override def onUpdate() {
    if ( ! isComplete) {
      super.onUpdate()
    }
  }
}
