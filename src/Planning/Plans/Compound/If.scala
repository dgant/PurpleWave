package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plan

class If(
  initialPredicate : Plan = NoPlan(),
  initialWhenTrue  : Plan = NoPlan(),
  initialWhenFalse : Plan = NoPlan())
    extends Plan {
  
  description.set("If")
  
  val predicate = new Property[Plan](initialPredicate)
  val whenTrue  = new Property[Plan](initialWhenTrue)
  val whenFalse = new Property[Plan](initialWhenFalse)
  
  override def getChildren: Iterable[Plan] = {
    Vector(predicate.get, whenTrue.get, whenFalse.get)
  }
  
  override def isComplete: Boolean = {
    predicate.get.isComplete && whenTrue.get.isComplete
  }
  
  override def onUpdate() {
    delegate(predicate.get)
    if (predicate.get.isComplete)
      delegate(whenTrue.get)
    else
      delegate(whenFalse.get)
  }
  
  override def toString: String = super.toString + ": " + predicate.get.toString
}
