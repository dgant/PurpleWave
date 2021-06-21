package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate

case class WeAreZerg() extends Predicate {
  override def apply: Boolean = With.self.isZerg
}
