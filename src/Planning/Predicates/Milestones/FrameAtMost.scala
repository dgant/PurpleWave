package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class FrameAtMost(frame: Int) extends Predicate {
  
  override def apply: Boolean = With.frame <= frame
  
}
