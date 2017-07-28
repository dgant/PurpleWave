package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan

class DefendHearts extends Plan {

  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWarriors)
  defenders.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    if (With.geography.ourBases.isEmpty) {
      return
    }
    
    val base = With.geography.ourBases.minBy(_.heart.tileDistanceFast(With.intelligence.mostBaselikeEnemyTile))
    
    defenders.get.acquire(this)
    defenders.get.units.foreach(
      _.agent.intend(this, new Intention {
        toReturn  = Some(base.heart.pixelCenter)
        toTravel  = Some(base.heart.pixelCenter)
      }))
  }
}