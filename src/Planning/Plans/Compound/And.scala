package Planning.Plans.Compound

import Planning.Predicate

class And(children: Predicate*) extends Predicate {
  
  override def isComplete: Boolean = children.forall(_.isComplete)
  
}