package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class FrameAtLeast(frame: Int) extends Predicate {
  
  override def isComplete: Boolean = With.frame >= frame
  
}
