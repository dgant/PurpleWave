package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class Trigger(
  initialPredicate : Plan = new Plan,
  initialWhenTrue  : Plan = new Plan,
  initialWhenFalse : Plan = new Plan)
  extends Plan {
  
  description.set("When triggered")
  
  val predicate = new Property[Plan](initialPredicate)
  val whenTrue  = new Property[Plan](initialWhenTrue)
  val whenFalse = new Property[Plan](initialWhenFalse)
  
  var triggered: Boolean = false
  
  override def getChildren: Iterable[Plan] = Vector(predicate.get, whenTrue.get, whenFalse.get)
  
  override def onUpdate() {
    delegate(predicate.get)
    triggered = triggered || predicate.get.isComplete
      
    if (triggered)
      delegate(whenTrue.get)
    else
      delegate(whenFalse.get)
  }
}
