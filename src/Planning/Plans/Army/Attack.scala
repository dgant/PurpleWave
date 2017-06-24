package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Intent.Intention
import Planning.Composition.UnitMatchers.UnitMatchWarriors

class Attack extends ControlPixel {
  
  controllers.get.unitMatcher.set(UnitMatchWarriors)
  
  override def onUpdate() {
  
    controllers.get.acquire(this)
    val target = getTarget
    updateTarget(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
    
    if (controllers.get.satisfied) {
      controllers.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) { toTravel = Some(targetPixel) }))
    }
  }
  
  protected def getTarget: Pixel = {
    
    if (With.geography.enemyBases.isEmpty) return With.intelligence.mostBaselikeEnemyTile.pixelCenter
    
    With.geography.enemyBases.head.heart.pixelCenter
  }
}
