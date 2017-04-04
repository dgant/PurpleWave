package Planning.Plans.Army

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.PositionFinder
import Planning.Composition.PositionFinders.Tactics.PositionEnemyBase
import Planning.Composition.Property
import Planning.Plan
import Planning.Composition.ResourceLocks.LockUnits
import Lifecycle.With
import Utilities.EnrichPosition._

class ControlPosition extends Plan {
  
  description.set("Control a position")
  
  val units = new Property[LockUnits](new LockUnits)
  var positionToControl = new Property[PositionFinder](new PositionEnemyBase)
  
  override def onFrame() {
    
    var targetPosition = positionToControl.get.find.get
    
    val ourBases = With.geography.ourBases.map(_.townHallArea.midPixel)
    val infiltrators = With.units.enemy
      .filter(e =>
        e.possiblyStillThere &&
        e.canAttackThisFrame &&
        ourBases.exists(base =>
          targetPosition.pixelCenter.pixelDistance(e.pixelCenter) <
          targetPosition.pixelCenter.pixelDistance(base)))
        
    if (infiltrators.nonEmpty) {
      targetPosition = infiltrators.map(_.tileCenter).minBy(_.tileDistance(With.geography.home))
    }
    
    units.get.acquire(this)
    if (units.get.satisfied) {
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = Some(targetPosition) }))
    }
  }
  
  override def drawOverlay() {
    
    positionToControl.get.find.map(tile => {
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
