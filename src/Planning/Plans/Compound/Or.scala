package Planning.Plans.Compound

import Planning.Plan

class Or(initialChildren: Plan*) extends Parallel(initialChildren: _*) {
  
  override def isComplete: Boolean = getChildren.exists(_.isComplete)
  
  override def toString: String = "(" + children.get.map(_.toString).mkString(" OR ") + ")"
}