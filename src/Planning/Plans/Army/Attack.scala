package Planning.Plans.Army

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobileDetectors, UnitMatchMobile, UnitMatchWarriors}
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Plan

class Attack extends Plan {
  
  description.set("Attack")
  
  val attackers = new Property[LockUnits](new LockUnits)
  attackers.get.unitMatcher.set(UnitMatchWarriors)
  attackers.get.unitCounter.set(UnitCountEverything)
  
  val detectors = new Property[LockUnits](new LockUnits)
  detectors.get.unitMatcher.set(UnitMatchAnd(UnitMatchMobileDetectors, UnitMatchMobile))
  detectors.get.unitCounter.set(UnitCountEverything)
  
  override def onUpdate() {
    
    val target =
      if (With.geography.enemyBases.isEmpty)
        With.intelligence.mostBaselikeEnemyTile
          .pixelCenter
      else
        With.geography.enemyBases
          .minBy(base =>
            if (With.geography.ourBases.nonEmpty)
              With.geography.ourBases.map(_.zone.distancePixels(base.zone)).min
            else
              - base.mineralsLeft)
            .heart.pixelCenter
    
    attackers.get.unitPreference.set(UnitPreferClose(target))
    attackers.get.acquire(this)
    
    if (attackers.get.units.isEmpty) return
  
    detectors.get.unitPreference.set(UnitPreferClose(target))
    detectors.get.satisfied
    detectors.get.acquire(this)
    
    attackers.get.units.foreach(attacker =>
      attacker.intend(
        new Intention(this) {
          toTravel = Some(target)
          canPursue = false
        }))
    
    var nextDetectorDepth = 32.0 * 4
    detectors.get.units.foreach(detector => {
      detector.intend(
        new Intention(this) {
          toTravel = Some(attackers.get.units.minBy(_.framesToTravel(target)).pixelCenter.project(target, nextDetectorDepth))
          canCower = true
        })
      nextDetectorDepth -= 96.0
    })
  }
}
