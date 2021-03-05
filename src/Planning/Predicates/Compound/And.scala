package Planning.Predicates.Compound

import Planning.Predicate

class And(children: Predicate*) extends Predicate {
  
  override def apply: Boolean = children.forall(_.apply)
  
}