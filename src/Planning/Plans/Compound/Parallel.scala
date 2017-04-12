package Planning.Plans.Compound

import Planning.Plan

class Parallel(initialChildren:Plan*) extends AbstractAll(initialChildren: _*) {
  
  description.set("Do in parallel")
  
  override def update() = getChildren.foreach(_.update())
}
