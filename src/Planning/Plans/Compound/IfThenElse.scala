package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class IfThenElse(
  initialPredicate : Plan = new Plan,
  initialWhenTrue  : Plan = new Plan,
  initialWhenFalse : Plan = new Plan)
    extends Plan {
  
  description.set("If/Then/Else")
  
  val predicate = new Property[Plan](initialPredicate)
  val whenTrue  = new Property[Plan](initialWhenTrue)
  val whenFalse = new Property[Plan](initialWhenFalse)
  
  override def getChildren: Iterable[Plan] = Vector(predicate.get, whenTrue.get, whenFalse.get)
  override def isComplete: Boolean = predicate.get.isComplete && whenTrue.get.isComplete
  
  override def onUpdate() {
    delegate(predicate.get)
    if (predicate.get.isComplete)
      delegate(whenTrue.get)
    else
      delegate(whenFalse.get)
  }
}
