package Planning.Plans.Compound

import Planning.Plan

class Parallel(initialChildren: Plan*) extends AbstractAll(initialChildren: _*) {
  
  override def onUpdate() { getChildren.foreach(delegate) }
}
