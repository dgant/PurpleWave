package Planning.Composition

import Lifecycle.With
import Planning.Plan
import Planning.Plans.Compound.NoPlan
import Utilities.Forever

class Latch(initialPredicate: Plan = NoPlan(), duration: Int = Forever()) extends Plan {
  
  val predicate = new Property[Plan](initialPredicate)
  
  private var lastCompletedFrame: Int = - Forever()
  
  override def isComplete: Boolean = {
    if (predicate.get.isComplete) {
      lastCompletedFrame = With.frame
    }
    lastCompletedFrame > 0 && With.framesSince(lastCompletedFrame) < duration
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
