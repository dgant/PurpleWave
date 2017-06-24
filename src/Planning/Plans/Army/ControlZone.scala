package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBattle
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan

class ControlZone(zone: Zone) extends Plan {
  
  val army = new Property[LockUnits](new LockUnits)
  
  override def onUpdate() {
    val battle = With.battles.byZone(zone)
    
    val enemies = battle.enemy.units
    val ourBase = zone.bases.find(_.owner.isUs)
    
    if (enemies.exists(threat => threat.canAttackThisSecond && ! threat.unitClass.isWorker)) {
      
      val threat = battle.estimationAbstract.avatarEnemy
      
      army.get.unitMatcher.set(UnitMatchWarriors)
      army.get.unitCounter.set(new UnitCountBattle(enemies, alwaysAccept = ourBase.isDefined))
      army.get.unitPreference.set(new UnitPreferClose(zone.centroid.pixelCenter))
      army.get.acquire(this)
      
      val target = enemies
        .map(_.pixelCenter)
        .minBy(_.pixelDistanceFast(ourBase.map(_.heart).getOrElse(zone.centroid).pixelCenter))
      
      army.get.units.foreach(unit => With.executor.intend(new Intention(this, unit) {
        toTravel = Some(target)
      }))
    }
    
    // TODO: If there's no threat, answer likely ones
  }
}
