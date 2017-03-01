package Plans.Army

import Global.Combat.Commands.Control
import Plans.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Types.Intents.Intention
import Utilities.Property

class PressureEnemyBaseFulfiller extends Plan {
  
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
  override def getChildren: Iterable[Plan] = { List(fighters.get) }
  override def onFrame() {
 
    val targetPosition = With.intelligence.mostBaselikeEnemyBuilding.map(_.position)
    if (targetPosition.isEmpty) return
    
    fighters.get.onFrame()
    if ( ! fighters.get.isComplete) return
    
    fighters.get.units.foreach(fighter => With.commander.intend(new Intention(fighter, Control, targetPosition)))
  }
}
