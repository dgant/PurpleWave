package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class DefendHearts extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
    
    val infiltrators =
      With.geography.ourBases
        .map(_.harvestingArea)
        .flatten(With.units.inRectangle)
        .filter(_.isBeingViolent)
    
    val spot =
      if (infiltrators.isEmpty)
        With.geography.ourBases
        .filter(_.mineralsLeft > 0)
        .toList
        .map(_.heart)
        .sortBy(_.tileDistanceSquared(With.intelligence.mostBaselikeEnemyTile))
        .headOption
        .getOrElse(With.geography.home)
        .pixelCenter
      else
        infiltrators
          .minBy(_.pixelDistanceFast(With.geography.home.pixelCenter)) //Kind of goofy but whatever
          .pixelCenter
    
    controllers.get.acquire(this)
    if (controllers.get.satisfied) {
      controllers.get.units.foreach(
        defender => {
          With.executor.intend(new Intention(this, defender) {
            toReturn  = Some(spot)
            toTravel  = Some(spot)
            canFlee   = false
          })
        })
    }
  }
}
