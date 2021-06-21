package Planning.Predicates

import Planning.Predicate

case class Never() extends Predicate {
  override def apply: Boolean = false
}
