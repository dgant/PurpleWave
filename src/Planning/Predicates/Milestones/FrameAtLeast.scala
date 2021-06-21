package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

case class FrameAtLeast(frame: () => Int) extends Predicate {

  def this(specificFrame: Int) = this(() => specificFrame)

  override def apply: Boolean = With.frame >= frame()
  
}
