package Planning.Plans.Army

import Lifecycle.With
import Micro.Formations.Formation
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Defend extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
    val chokeOption = With.geography.mostExposedChokes.headOption
    val pixelToDefend = chokeOption.map(_.centerPixel).getOrElse(With.geography.home.pixelCenter)
    control(pixelToDefend)
    chokeOption.foreach(choke =>
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
    
  }
}
