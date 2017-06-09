package Planning.Plans.Compound

import Planning.Plan

class Or(initialChildren: Plan*) extends Parallel(initialChildren: _*) {
  
  override def isComplete: Boolean = getChildren.exists(_.isComplete)
}