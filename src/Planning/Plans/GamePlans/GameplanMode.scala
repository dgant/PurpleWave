package Planning.Plans.GamePlans

import Planning.{Plan, Predicate}
import Planning.Plans.Compound.Parallel
import Planning.Predicates.{Always, Never}

abstract class GameplanMode extends Parallel {
  
  override def isComplete: Boolean = completionCriteria.isComplete || ! activationCriteria.isComplete
  
  val activationCriteria: Predicate = new Always
  val completionCriteria: Predicate = new Never
  
  override def onUpdate() {
    if ( ! isComplete) {
      super.onUpdate()
    }
  }
}
