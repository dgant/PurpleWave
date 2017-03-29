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
    
    var targetPosition = position.get.find.get
    
    val ourBases = With.geography.ourBases.map(_.townHallRectangle.midPixel)
    val infiltrators = With.units.enemy
      .filter(e =>
        e.possiblyStillThere &&
        e.canAttack &&
        ourBases.exists(base =>
          targetPosition.pixelCenter.pixelDistance(e.pixelCenter) <
          targetPosition.pixelCenter.pixelDistance(base)))
        
    if (infiltrators.nonEmpty) {
      targetPosition = infiltrators.map(_.tileCenter).minBy(_.tileDistance(With.geography.home))
    }
    
    units.get.onFrame()
    if (units.get.isComplete) {
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = Some(targetPosition) }))
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
