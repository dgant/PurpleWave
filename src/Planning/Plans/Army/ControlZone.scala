package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.{UnitCountCombat, UnitCountOne}
import Planning.Composition.UnitMatchers._
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}

class ControlZone(zone: Zone) extends Plan {
  
  val fighters = new Property[LockUnits](new LockUnits)
  fighters.get.unitMatcher.set(UnitMatchWarriors)
  fighters.get.unitPreference.set(UnitPreferClose(zone.centroid.pixelCenter))
  
  val detectors = new Property[LockUnits](new LockUnits)
  detectors.get.unitPreference.set(fighters.get.unitPreference.get)
  detectors.get.unitMatcher.set(UnitMatchAnd(UnitMatchMobileDetectors, UnitMatchMobile))
  detectors.get.unitCounter.set(UnitCountOne)
  
  var enemies: Set[ForeignUnitInfo] = Set.empty
  var threats: Set[ForeignUnitInfo] = Set.empty
  
  override def onUpdate() {
  
    val ourBase = zone.bases.find(base => base.owner.isUs)
    
    enemies = With.units.enemy.filter(enemy => enemy.likelyStillThere && threateningZone(enemy, zone))
    threats = enemies.filter(threat => threat.canAttackThisSecond && ! threat.unitClass.isWorker && threat.likelyStillThere)
    if (threats.nonEmpty) {
      fighters.get.unitMatcher.set(UnitMatchCombat(enemies))
      fighters.get.unitCounter.set(new UnitCountCombat(enemies, alwaysAccept = ourBase.isDefined))
      fighters.get.acquire(this)
      
      if (fighters.get.satisfied) {
        val target = threats
          .map(_.pixelCenter)
          .minBy(_.pixelDistanceFast(ourBase.map(_.heart).getOrElse(zone.centroid).pixelCenter))
  
        fighters.get.units.foreach(_.intend(new Intention(this) {
          toTravel = Some(target)
          canPursue = true
        }))
  
        val cloakedThreats = threats.filter(threat => threat.cloaked || threat.burrowed)
        if (cloakedThreats.nonEmpty) {
          detectors.get.acquire(this)
          detectors.get.units.foreach(_.intend(new Intention(this) {
            toTravel = Some(cloakedThreats.minBy(_.pixelDistanceFast(zone.centroid.pixelCenter)).pixelCenter)
            canCower = true
          }))
        }
      }
    }
    
    // TODO: If there's no threat, answer likely ones
  }
  
  def threateningZone(unit: UnitInfo, zone: Zone): Boolean = {
    val unitZone = unit.pixelCenter.zone
    zone == unitZone ||
    (unit.flying && unit.framesToTravelTo(zone.centroid.pixelCenter) < 24 * 8) ||
    zone.edges.exists(edge => edge.otherSideof(zone).owner != With.self && edge.otherSideof(zone) == unitZone)
  }
}
