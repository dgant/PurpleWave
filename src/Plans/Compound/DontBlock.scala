package Plans.Compound

import Plans.Plan
import Types.Property

class DontBlock extends Plan {
  
  description.set(Some("Dont wait for completion"))
  
  val child = new Property[Plan](new Plan)
  
  override def isComplete: Boolean = true
  override def getChildren: Iterable[Plan] = { List (child.get) }
  override def onFrame() = { child.get.onFrame() }
}
