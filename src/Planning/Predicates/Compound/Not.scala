package Planning.Predicates.Compound

import Planning.Predicate

class Not(child: Predicate) extends Predicate {
  
  override def apply: Boolean = ! child.apply
  
}
