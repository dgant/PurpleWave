package Planning.Plans.Basic

import Lifecycle.With
import Planning.Plan

class WriteStatus(text: () => String) extends Plan {

  def this(fixedText: String) {
    this(() => fixedText)
  }

  override def onUpdate(): Unit = {
    With.blackboard.status.set(With.blackboard.status() :+ text())
  }
}
