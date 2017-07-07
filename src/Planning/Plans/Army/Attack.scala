package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchDetectors, UnitMatchMobile, UnitMatchWarriors}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan

class Attack extends Plan {
  
  description.set("Attack")
  
  val attackers = new Property[LockUnits](new LockUnits)
  attackers.get.unitMatcher.set(UnitMatchWarriors)
  attackers.get.unitCounter.set(UnitCountEverything)
  
  val detectors = new Property[LockUnits](new LockUnits)
  detectors.get.unitMatcher.set(UnitMatchAnd(UnitMatchDetectors, UnitMatchMobile))
  detectors.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    val target =
      if (With.geography.enemyBases.isEmpty)
        With.intelligence.mostBaselikeEnemyTile
          .pixelCenter
      else
        With.geography.enemyBases
          .map(_.heart.pixelCenter)
          .minBy(_.groundPixels(With.geography.home))
    
    attackers.get.unitPreference.set(UnitPreferClose(target))
    attackers.get.acquire(this)
    
    if (attackers.get.units.isEmpty) return
  
    detectors.get.unitPreference.set(UnitPreferClose(target))
    detectors.get.satisfied
    detectors.get.acquire(this)
    
    attackers.get.units.foreach(attacker =>
      With.executor.intend(
        new Intention(this, attacker) {
          toTravel = Some(target)
          canPursue = false
        }))
    
    var nextDetectorDepth = 32.0 * 4
    detectors.get.units.foreach(detector => {
      With.executor.intend(
        new Intention(this, detector) {
          toTravel = Some(attackers.get.units.minBy(_.framesToTravel(target)).pixelCenter.project(target, nextDetectorDepth))
          canCower = true
        })
      nextDetectorDepth -= 96.0
    })
  }
}
