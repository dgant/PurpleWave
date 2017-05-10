package Planning.Plans.Compound

import Planning.Plan

class If(lambda:() => Boolean) extends Plan {
  
  description.set("If (lambda)")
  
  override def isComplete: Boolean = lambda()
}
