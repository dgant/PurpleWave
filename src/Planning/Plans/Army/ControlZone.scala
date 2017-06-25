package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountCombat
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.UnitInfo

class ControlZone(zone: Zone) extends Plan {
  
  val army = new Property[LockUnits](new LockUnits)
  
  override def onUpdate() {
    
    val enemies = With.units.enemy.filter(threateningZone)
    val ourBase = zone.bases.find(_.owner.isUs)
    
    if (enemies.exists(threat => threat.canAttackThisSecond && ! threat.unitClass.isWorker)) {
      army.get.unitMatcher.set(UnitMatchWarriors)
      army.get.unitCounter.set(new UnitCountCombat(enemies, alwaysAccept = ourBase.isDefined))
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
  
  def threateningZone(unit: UnitInfo): Boolean = {
    unit.pixelCenter.zone == zone                   ||
    unit.targetPixel.exists(_.zone == zone)         ||
    unit.target.exists(_.pixelCenter.zone == zone)  ||
    zone.edges.map(_.centerPixel).exists(unit.pixelDistanceFast(_) < With.configuration.battleMarginPixels)
  }
}
