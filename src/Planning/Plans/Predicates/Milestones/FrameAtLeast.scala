package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan

class FrameAtLeast(frame: Int) extends Plan {
  
  override def isComplete: Boolean = With.frame >= frame
  
}
