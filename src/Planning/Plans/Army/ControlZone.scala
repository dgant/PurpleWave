package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBattle
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan

class ControlZone(zone: Zone) extends Plan {
  
  val army = new Property[LockUnits](new LockUnits)
  
  override def onUpdate() {
    val battle = With.battles.byZone(zone)
    
    // If there's a threat, answer it
    if (battle.enemy.units.exists(threat => threat.canAttackThisSecond && ! threat.unitClass.isWorker)) {
      
      val threat = battle.estimationAbstract.avatarEnemy
      
      army.get.unitCounter.set(new UnitCountBattle(threat))
      army.get.unitPreference.set(new UnitPreferClose(zone.centroid.pixelCenter))
    }
    
    // TODO: If there's no threat, answer likely ones
  }
}
