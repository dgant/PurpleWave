package Planning.Plans.Gameplans.All

import Planning.Plan

class ModalGameplan(modes: Modal*) extends Plan with Modal {
  override def isComplete: Boolean = modes.forall(_.isComplete)
  override def onUpdate() {
    modes.find( ! _.isComplete).foreach(_.update())
  }
}