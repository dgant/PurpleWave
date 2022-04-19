package Planning.Plans.Compound

import Planning.Plans.Basic.NoPlan
import Planning.Predicates.{Never, Predicate}
import Planning.Plan
import Utilities.Property

class If(
  initialPredicate : Predicate = new Never,
  initialWhenTrue  : Plan = NoPlan(),
  initialWhenFalse : Plan = NoPlan())
    extends Plan {
  
  val predicate = new Property[Predicate](initialPredicate)
  val whenTrue  = new Property[Plan](initialWhenTrue)
  val whenFalse = new Property[Plan](initialWhenFalse)
  
  override def onUpdate() {
    if (predicate.get.apply)
      whenTrue.get.update()
    else
      whenFalse.get.update()
  }
}
