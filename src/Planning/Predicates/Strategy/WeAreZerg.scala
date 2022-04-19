package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicates.Predicate

case class WeAreZerg() extends Predicate {
  override def apply: Boolean = With.self.isZerg
}
