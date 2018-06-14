package Planning.Plans.Compound

import Planning.Predicate

class And(initialChildren: Predicate*) extends Predicate {
  
  final override val getChildren: Iterable[Predicate] = initialChildren
  
  override def isComplete: Boolean = getChildren.forall(_.isComplete)
  
  override def toString: String = "(" + getChildren.map(_.toString).mkString(" AND ") + ")"
}