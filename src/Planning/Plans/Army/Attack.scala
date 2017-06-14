package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Attack extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
    updateTarget(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    controllers.get.acquire(this)
    if (controllers.get.satisfied) {
      controllers.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { toTravel = Some(targetPixel) }))
    }
  }
}
