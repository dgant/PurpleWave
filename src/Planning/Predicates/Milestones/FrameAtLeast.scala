package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class FrameAtLeast(frame: () => Int) extends Predicate {

  def this(specificFrame: Int) = this(() => specificFrame)

  override def isComplete: Boolean = With.frame >= frame()
  
}
