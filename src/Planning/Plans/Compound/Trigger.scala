package Planning.Plans.Compound

import Planning.Plans.Basic.NoPlan
import Planning.Predicates.Compound.Latch
import Planning.Predicates.{Never, Predicate}
import Planning.Plan
import Utilities.Property

class Trigger(
  initialPredicate : Predicate = new Never,
  initialAfter     : Plan = NoPlan(),
  initialBefore    : Plan = NoPlan())
    extends Plan {
  
  val predicate = new Property[Predicate](initialPredicate)
  val after     = new Property[Plan](initialAfter)
  val before    = new Property[Plan](initialBefore)
  val latch     = new Latch
  
  latch.predicate.inherit(predicate)
  
  var triggered: Boolean = false
  
  override def onUpdate() {
    triggered = triggered || latch.apply
    if (triggered)
      after.get.update()
    else
      before.get.update()
  }
}
