package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan

class Attack extends Plan {
  
  description.set("Attack")
  
  val attackers  = new Property[LockUnits](new LockUnits)
  attackers.get.unitMatcher.set(UnitMatchWarriors)
  attackers.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    val target =
      if (With.geography.enemyBases.isEmpty)
        With.intelligence.mostBaselikeEnemyTile
          .pixelCenter
      else
        With.geography.enemyBases
          .map(_.heart.pixelCenter)
          .minBy(_.groundPixels(With.geography.home))
    
    attackers.get.unitPreference.set(new UnitPreferClose(target))
    attackers.get.acquire(this)
    
    attackers.get.units.foreach(fighter =>
      With.executor.intend(
        new Intention(this, fighter) {
          toTravel = Some(target)
          canPursue = false
        }))
  }
}
