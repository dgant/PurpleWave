package Planning.Plans.GamePlans

import Planning.Plan

class ModalGameplan(modes: Modal*) extends Plan with Modal {
  override def completed: Boolean = modes.forall(_.completed)
  override def onUpdate() {
    modes.find( ! _.completed).foreach(_.update())
  }
}