package Planning.Plans.Compound

import Planning.Predicate

class Not(child: Predicate) extends Predicate {
  
  override def isComplete: Boolean = ! child.isComplete
  
}
