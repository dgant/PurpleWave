package Planning.Predicates.Compound

import Lifecycle.With
import Planning.Predicates.{Never, Predicate}
import Utilities.Property
import Utilities.Time.Forever

case class Latch(initialPredicate: Predicate = new Never, duration: Int = Forever()) extends Predicate {
  
  val predicate = new Property[Predicate](initialPredicate)
  
  private var lastCompletedFrame: Int = - Forever()
  
  override def apply: Boolean = {
    if (predicate.get.apply) {
      lastCompletedFrame = With.frame
    }
    lastCompletedFrame > 0 && With.framesSince(lastCompletedFrame) < duration
  }
}
