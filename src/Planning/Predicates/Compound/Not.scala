package Planning.Predicates.Compound

import Planning.Predicate

case class Not(child: Predicate) extends Predicate {
  override def apply: Boolean = ! child.apply
}
