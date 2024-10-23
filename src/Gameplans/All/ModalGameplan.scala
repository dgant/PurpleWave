package Gameplans.All

import Planning.Plan

class ModalGameplan(modes: Modal*) extends Plan with Modal {
  override def isComplete: Boolean = modes.forall(_.isComplete)
  override def onUpdate(): Unit = {
    modes.find( ! _.isComplete).foreach(_.update())
  }
}