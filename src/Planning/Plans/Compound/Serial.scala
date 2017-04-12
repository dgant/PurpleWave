package Planning.Plans.Compound

import Planning.Plan

class Serial(initialChildren:Plan*) extends AbstractAll(initialChildren: _*) {
  
  description.set("Do in series")
  
  override def update() {
    var continue = true
    getChildren.foreach(child => if (continue) { child.update(); continue = child.isComplete })
  }
}
