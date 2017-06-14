package Planning.Plans.Army

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.{Plan, Yolo}

abstract class ControlPixel extends Plan {
  
  description.set("Control a position")
  
  val controllers = new Property[LockUnits](new LockUnits)
  
  private val infiltrationRadius = 32.0 * 25
  
  protected override def onUpdate()
  
  protected var targetPixel: Pixel = SpecificPoints.middle
  
  protected def control(pixel: Pixel) {
    
    targetPixel = pixel
    val ourBases = With.geography.ourBases.map(_.townHallArea.midPixel)
    val infiltrators = With.units.enemy
      .filter(e =>
        ! Yolo.active &&
        e.visible &&
        e.canAttackThisSecond &&
        ourBases.exists(base =>
          e.pixelDistanceTravelling(base.tileIncluding) < infiltrationRadius &&
          e.pixelDistanceTravelling(base.tileIncluding) <
          e.pixelDistanceTravelling(base.tileIncluding, targetPixel.tileIncluding)))
        
    if (infiltrators.nonEmpty) {
      targetPixel = infiltrators.map(_.pixelCenter).minBy(_.pixelDistanceSlow(With.geography.home.pixelCenter))
    }
    
    controllers.get.acquire(this)
    if (controllers.get.satisfied) {
      controllers.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { toTravel = Some(targetPixel) }))
    }
  }
  
  override def visualize() {
    DrawMap.circle(
      targetPixel,
      64,
      With.self.colorDark)
        
    DrawMap.label(
      description.get,
      targetPixel,
      drawBackground = true,
      With.self.colorDark)
  }
}
