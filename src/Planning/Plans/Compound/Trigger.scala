package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Trigger(
  initialTrigger  : Plan = NoPlan(),
  initialAfter    : Plan = NoPlan(),
  initialBefore   : Plan = NoPlan())
    extends Plan {
  
  description.set("Trigger when")
  
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
  
  override def toString: String = super.toString + ": " + trigger.get.toString
}
