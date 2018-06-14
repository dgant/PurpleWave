package Planning.Plans.Compound

import Planning.Predicate

class Or(children: Predicate*) extends Predicate{
  
  override def isComplete: Boolean = children.exists(_.isComplete)
}