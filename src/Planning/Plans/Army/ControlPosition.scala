package Planning.Plans.Army

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.PixelFinders.Tactics.TileEnemyBase
import Planning.Composition.PixelFinders.TileFinder
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Plan

class ControlPixel extends Plan {
  
  description.set("Control a position")
  
  val infiltrationRadius = 32.0 * 25
  
  val units = new Property[LockUnits](new LockUnits)
  var positionToControl = new Property[TileFinder](new TileEnemyBase)
  
  override def update() {
    
    var targetTile = positionToControl.get.find.get
    
    val ourBases = With.geography.ourBases.map(_.townHallArea.midPixel)
    val infiltrators = With.units.enemy
      .filter(e =>
        e.possiblyStillThere &&
        e.canAttackThisSecond &&
        ourBases.exists(base =>
          e.travelPixels(base.tileIncluding) < infiltrationRadius &&
          e.travelPixels(base.tileIncluding) <
          e.travelPixels(base.tileIncluding, targetTile)))
        
    if (infiltrators.nonEmpty) {
      targetTile = infiltrators.map(_.tileIncludingCenter).minBy(_.tileDistanceSlow(With.geography.home))
    }
    
    units.get.acquire(this)
    if (units.get.satisfied) {
      //TODO: Dispatch only units capable of fighting an infiltrator
      
      units.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { destination = Some(targetTile.pixelCenter) }))
    }
  }
  
  override def drawOverlay() {
    
    positionToControl.get.find.map(tile => {
      DrawMap.circle(
        tile.pixelCenter,
        64,
        With.self.colorDark)
      
      DrawMap.label(
        description.get,
        tile.pixelCenter,
        drawBackground = true,
        With.self.colorDark)
    })
  }
}
