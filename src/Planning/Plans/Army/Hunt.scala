package Planning.Plans.Army

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.{PositionEnemyBase, PositionFinder}
import Planning.Composition.Property
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import Startup.With
import Utilities.EnrichPosition._

class Hunt extends Plan {
  
  val hunters = new Property[LockUnits](new LockUnits)
  var position = new Property[PositionFinder](new PositionEnemyBase)
  
  override def getChildren: Iterable[Plan] = List(hunters.get)
  override def onFrame() {
    
    val targetPosition = position.get.find
    
    if (targetPosition.isEmpty) return
    
    hunters.get.onFrame()
    if (hunters.get.isComplete) {
      hunters.get.units.foreach(fighter => {
        val targets = With.units.enemy.filter(fighter.canAttack)
        val targetDestination = if(targets.isEmpty) targetPosition else targets.minBy(_.pixelDistanceSquared(fighter)).tileCenter
        With.executor.intend(new Intention(this, fighter) { destination = targetPosition })
      })
    }
  }
  
  override def drawOverlay() {
    
    position.get.find.map(tile => {
      DrawMap.circle(
        tile.pixelCenter,
        64,
        DrawMap.playerColor(With.self))
      
      DrawMap.label(
        description.get,
        tile.pixelCenter,
        drawBackground = true,
        DrawMap.playerColor(With.self))
    })
  }
}
