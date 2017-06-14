package Planning.Plans.Army

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Attack extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
    control(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  }
}
