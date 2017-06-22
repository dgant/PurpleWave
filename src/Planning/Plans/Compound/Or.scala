package Planning.Plans.Compound

import Planning.Plan

class Or(initialChildren: Plan*) extends Parallel(initialChildren: _*) {
  description.set("Or")
  override def isComplete: Boolean = getChildren.exists(_.isComplete)
}