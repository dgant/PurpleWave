package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Trigger(
  initialTrigger  : Plan = new Plan,
  initialAfter    : Plan = new Plan,
  initialBefore   : Plan = new Plan)
    extends Plan {
  
  description.set("When triggered")
  
  val trigger = new Property[Plan](initialTrigger)
  val after   = new Property[Plan](initialAfter)
  val before  = new Property[Plan](initialBefore)
  
  var triggered: Boolean = false
  
  override def getChildren: Iterable[Plan] = Vector(trigger.get, after.get, before.get)
  
  override def onUpdate() {
    delegate(trigger.get)
    triggered = triggered || trigger.get.isComplete
    if (triggered)
      delegate(after.get)
    else
      delegate(before.get)
  }
}
