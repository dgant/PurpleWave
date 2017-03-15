package Planning.Plans.Army

import Debugging.Visualization.DrawMap
import Micro.Behaviors.DefaultBehavior
import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.PositionFinders.{PositionEnemyBase, PositionFinder}
import Micro.Intentions.Intention
import Planning.Composition.Property
import Utilities.TypeEnrichment.EnrichPosition._

class ControlPosition extends Plan {
  
  val units = new Property[LockUnits](new LockUnits)
  var position = new Property[PositionFinder](new PositionEnemyBase)
  
  override def getChildren: Iterable[Plan] = List(units.get)
  override def onFrame() {
    
    val targetPosition = position.get.find
    
    if (targetPosition.isEmpty) return
    
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.commander.intend(new Intention(this, fighter, DefaultBehavior, targetPosition.get)))
    }
  }
  
  override def drawOverlay() {
    
    position.get.find.map(tile => {
      DrawMap.circle(
        tile.centerPixel,
        64,
        DrawMap.playerColor(With.self))
      
      DrawMap.label(
        description.get,
        tile.centerPixel,
        drawBackground = true,
        DrawMap.playerColor(With.self))
    })
  }
}
