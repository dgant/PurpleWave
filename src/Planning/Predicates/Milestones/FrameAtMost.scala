package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicates.Predicate

case class FrameAtMost(frame: Int) extends Predicate {
  override def apply: Boolean = With.frame <= frame
}
