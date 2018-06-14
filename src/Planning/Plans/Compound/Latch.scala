package Planning.Plans.Compound

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plans.Predicates.Never
import Planning.{Plan, Predicate}
import Utilities.Forever

class Latch(initialPredicate: Predicate = new Never, duration: Int = Forever()) extends Predicate {
  
  val predicate = new Property[Plan](initialPredicate)
  
  private var lastCompletedFrame: Int = - Forever()
  
  override def isComplete: Boolean = {
    if (predicate.get.isComplete) {
      lastCompletedFrame = With.frame
    }
    lastCompletedFrame > 0 && With.framesSince(lastCompletedFrame) < duration
  }
  
  override def getChildren: Iterable[Plan] = {
    Iterable(predicate.get)
  }
}
