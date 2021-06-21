package Planning.Predicates

import Planning.Predicate

case class Always() extends Predicate {
  override def apply: Boolean = true
}
