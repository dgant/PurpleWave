package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Formations.Formation
import Mathematics.Points.Pixel
import Micro.Intent.Intention
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.UnitInfo.UnitInfo

class Defend extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
    val chokeOption = With.geography.mostExposedChokes.headOption
    val pixelToDefend = chokeOption.map(_.centerPixel).getOrElse(With.geography.home.pixelCenter)
    updateTarget(pixelToDefend)
    
    val formation = chokeOption
      .map(choke =>
        Formation.concave(
          controllers.get.units,
          choke.sidePixels.head,
          choke.sidePixels.last,
          choke.zones
            .toList
            .sortBy(zone => With.paths.groundPixels(zone.centroid.tileIncluding, With.geography.home))
            .sortBy(zone => zone.owner != With.self)
            .head
            .centroid))
      .getOrElse(Map[UnitInfo, Pixel]())
  
    controllers.get.acquire(this)
    if (controllers.get.satisfied) {
      controllers.get.units.foreach(
        defender => {
          val spot = formation.get(defender).getOrElse(targetPixel)
          With.executor.intend(new Intention(this, defender) {
            toTravel = Some(spot)
            toForm  = Some(spot)
          })
        })
    }
  }
}
