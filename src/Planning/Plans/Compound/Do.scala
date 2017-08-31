package Planning.Plans.Compound

import Planning.Plan

class Do(lambda:() => Unit) extends Plan {
  
  description.set("Do (lambda)")
  
  override def onUpdate() { lambda() }
}
