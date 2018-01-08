package Planning.Composition

import Planning.Plan
import Planning.Plans.Compound.NoPlan

class Latch(initialPredicate: Plan = NoPlan()) extends Plan {
  
  val predicate = new Property[Plan](initialPredicate)
  
  private var completedOnce: Boolean = false
  
  override def isComplete: Boolean = {
    completedOnce = completedOnce || predicate.get.isComplete
    completedOnce
  }
  
  override def onUpdate(): Unit = {
    if ( ! isComplete) {
      predicate.get.update()
    }
  }
  
  override def getChildren: Iterable[Plan] = {
    Iterable(predicate.get)
  }
}
