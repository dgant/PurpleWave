package Planning.Plans.Basic

import Planning.Plan

class Do(lambda:() => Unit) extends Plan {
  override def onUpdate() { lambda() }
}
