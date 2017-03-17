package Planning.Plans.Army

import Micro.Intentions.Intention
import Planning.Composition.Property
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import Startup.With

class Defend extends Plan {
  
  val units = new Property[LockUnits](new LockUnits)
  
  override def getChildren: Iterable[Plan] = List(units.get)
  override def onFrame() {
    val attackers = With.units.enemy.filter(_.distance(With.geography.home) < 32 * 40)
    if (attackers.isEmpty) return
    val placeToDefend = attackers.minBy(_.distance(With.geography.home)).tileCenter
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = Some(placeToDefend) }))
    }
  }
}
