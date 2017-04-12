package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class IfThenElse extends Plan {
  
  description.set("If/Then/Else")
  
  val predicate = new Property[Plan](new Plan)
  val whenTrue  = new Property[Plan](new Plan)
  val whenFalse = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = Vector(predicate.get, whenTrue.get, whenFalse.get)
  override def isComplete: Boolean = predicate.get.isComplete && whenTrue.get.isComplete
  
  override def onFrame() {
    predicate.get.onFrame()
    if (predicate.get.isComplete)
      whenTrue.get.onFrame()
    else
      whenFalse.get.onFrame()
  }
}
