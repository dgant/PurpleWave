package Planning.Plans.Compound

import Planning.Plans.Basic.NoPlan
import Planning.Predicates.Never
import Planning.{Plan, Predicate, Property}

class FlipIf(
  initialPredicate  : Predicate = new Never,
  inititialFirst    : Plan = NoPlan(),
  initialSecond     : Plan = NoPlan())
    extends Plan {
  
  val predicate = new Property[Predicate](initialPredicate)
  val first  = new Property[Plan](inititialFirst)
  val second = new Property[Plan](initialSecond)
  
  override def onUpdate() {
    if (predicate.get()) {
      second.get.update()
      first.get.update()
    } else {
      first.get.update()
      second.get.update()
    }
  }
}
