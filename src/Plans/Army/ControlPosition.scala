package Plans.Army

import Global.Combat.Commands.Control
import Plans.Allocation.LockUnits
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionEnemyBase, PositionFinder}
import Types.Intents.Intention
import Utilities.Property

class ControlPosition extends Plan {
  
  val units = new Property[LockUnits](new LockUnits)
  var position = new Property[PositionFinder](new PositionEnemyBase)
  
  override def getChildren: Iterable[Plan] = List(units.get)
  override def onFrame() {
    
    val targetPosition = position.get.find
    
    if (targetPosition.isEmpty) return
    
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.commander.intend(new Intention(fighter, Control, targetPosition.get)))
    }
  }
}
