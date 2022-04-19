package Planning.Predicates.Compound

import Planning.Predicates.Predicate

case class And(children: Predicate*) extends Predicate {
  override def apply: Boolean = children.forall(_.apply)
}