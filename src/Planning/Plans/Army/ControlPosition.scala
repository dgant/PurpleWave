package Planning.Plans.Army

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.{PositionEnemyBase, PositionFinder}
import Planning.Composition.Property
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import Startup.With
import Utilities.EnrichPosition._

class ControlPosition extends Plan {
  
  description.set("Control a position")
  
  val units = new Property[LockUnits](new LockUnits)
  var position = new Property[PositionFinder](new PositionEnemyBase)
  
  override def getChildren: Iterable[Plan] = List(units.get)
  override def onFrame() {
    
    val targetPosition = position.get.find
    
    if (targetPosition.isEmpty) return
    
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = targetPosition }))
    }
  }
  
  override def drawOverlay() {
    
    position.get.find.map(tile => {
      DrawMap.circle(
        tile.pixelCenter,
        64,
        DrawMap.playerColorDark(With.self))
      
      DrawMap.label(
        description.get,
        tile.pixelCenter,
        drawBackground = true,
        DrawMap.playerColorDark(With.self))
    })
  }
}
