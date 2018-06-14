package Planning.Plans.Compound

import Planning.Composition.Property
import Planning.Plans.Predicates.Never
import Planning.{Plan, Predicate}

class Trigger(
  initialPredicate : Predicate = new Never,
  initialAfter     : Plan = NoPlan(),
  initialBefore    : Plan = NoPlan())
    extends Plan {
  
  description.set("Trigger when")
  
  val predicate = new Property[Predicate](initialPredicate)
  val after     = new Property[Plan](initialAfter)
  val before    = new Property[Plan](initialBefore)
  val latch     = new Latch
  
  latch.predicate.inherit(predicate)
  
  var triggered: Boolean = false
  
  override def getChildren: Iterable[Plan] = Vector(after.get, before.get)
  
  override def onUpdate() {
    triggered = triggered || latch.isComplete
    if (triggered)
      delegate(after.get)
    else
      delegate(before.get)
  }
}
