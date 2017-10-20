package Planning.Plans.GamePlans

import Planning.Plan
import Planning.Plans.Compound.AbstractAll

class ModalGameplan(modes: Plan*) extends AbstractAll(modes: _*) {
  
  description.set("Modal game plan")
  
  override def onUpdate() {
    val nextMode = children.get.find( ! _.isComplete)
    nextMode.foreach(delegate)
  }
}