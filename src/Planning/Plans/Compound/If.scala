package Planning.Plans.Compound

import Planning.Predicates.Never
import Planning.{Plan, Predicate, Property}

class If(
  initialPredicate : Predicate = new Never,
  initialWhenTrue  : Plan = NoPlan(),
  initialWhenFalse : Plan = NoPlan())
    extends Plan {
  
  description.set("If")
  
  val predicate = new Property[Predicate](initialPredicate)
  val whenTrue  = new Property[Plan](initialWhenTrue)
  val whenFalse = new Property[Plan](initialWhenFalse)
  
  override def getChildren: Iterable[Plan] = {
    Vector(whenTrue.get, whenFalse.get)
  }
  
  override def isComplete: Boolean = {
    predicate.get.isComplete && whenTrue.get.isComplete
  }
  
  override def onUpdate() {
    if (predicate.get.isComplete)
      delegate(whenTrue.get)
    else
      delegate(whenFalse.get)
  }
}
