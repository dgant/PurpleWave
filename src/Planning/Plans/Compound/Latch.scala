package Planning.Plans.Compound

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plans.Predicates.Never
import Planning.Predicate
import Utilities.Forever

class Latch(initialPredicate: Predicate = new Never, duration: Int = Forever()) extends Predicate {
  
  val predicate = new Property[Predicate](initialPredicate)
  
  private var lastCompletedFrame: Int = - Forever()
  
  override def isComplete: Boolean = {
    if (predicate.get.isComplete) {
      lastCompletedFrame = With.frame
    }
    lastCompletedFrame > 0 && With.framesSince(lastCompletedFrame) < duration
  }
}
