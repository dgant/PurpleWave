package Planning.Plans.Compound

import Planning.Plan

class Or(initialChildren:Plan*) extends Parallel(initialChildren: _*) {
  
  description.set("And")
  
  override def isComplete: Boolean = children.get.exists(_.isComplete)
}