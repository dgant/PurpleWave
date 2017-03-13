package Plans.Army

import Global.Combat.Behaviors.DefaultBehavior
import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Types.Intents.Intention
import Utilities.Property

class Defend extends Plan {
  
  val units = new Property[LockUnits](new LockUnits)
  
  override def getChildren: Iterable[Plan] = List(units.get)
  override def onFrame() {
    val attackers = With.units.enemy.filter(_.distance(With.geography.home) < 32 * 40)
    if (attackers.isEmpty) return
    val destination = attackers.minBy(_.distance(With.geography.home)).tileCenter
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.commander.intend(new Intention(this, fighter, DefaultBehavior, destination)))
    }
  }
}
