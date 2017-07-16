package Planning.Plans.Army

import Information.Geography.Types.Zone
import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.{UnitCountCombat, UnitCountOne}
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobileDetectors, UnitMatchMobile, UnitMatchWarriors}
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
  
    val ourBase = zone.bases.find(base => base.owner.isUs || base.planningToTake)
    
    val zonesToConsider =
      if(ourBase.isDefined)
        Vector(zone)
      else {
        // This sequence broke my brain so let's lay out the steps and let static type analysis save us
        val aaa = With.geography.home.zone.pathTo(zone)
        val bbb = aaa.map(_.steps)
        val ccc = bbb.map(_.map(_.from))
        val ddd = ccc.getOrElse(Vector.empty)
        ddd
      }
    
    enemies = With.units.enemy.filter(enemy => enemy.likelyStillThere && threateningZone(enemy, zonesToConsider))
    threats = enemies.filter(threat => threat.canAttackThisSecond && ! threat.unitClass.isWorker && threat.likelyStillThere)
    if (threats.nonEmpty) {
      fighters.get.unitCounter.set(new UnitCountCombat(enemies, alwaysAccept = ourBase.isDefined))
      fighters.get.acquire(this)
      
      if (fighters.get.satisfied) {
        val target = threats
          .map(_.pixelCenter)
          .minBy(_.pixelDistanceFast(ourBase.map(_.heart).getOrElse(zone.centroid).pixelCenter))
  
        fighters.get.units.foreach(fighter => With.executor.intend(new Intention(this, fighter) {
          toTravel = Some(target)
        }))
  
        val cloakedThreats = threats.filter(threat => threat.cloaked || threat.burrowed)
        if (cloakedThreats.nonEmpty) {
          detectors.get.acquire(this)
          detectors.get.units.foreach(detector => With.executor.intend(new Intention(this, detector) {
            toTravel = Some(cloakedThreats.minBy(_.pixelDistanceFast(zone.centroid.pixelCenter)).pixelCenter)
            canCower = true
          }))
        }
      }
    }
    
    // TODO: If there's no threat, answer likely ones
  }
  
  def threateningZone(unit: UnitInfo, zones: Vector[Zone]): Boolean = {
    zones.contains(unit.pixelCenter.zone)   ||
    unit.targetPixel.exists(zones.contains) ||
    unit.target.exists(zones.contains)      ||
    zones
      .flatMap(_.edges)
      .map(_.centerPixel)
      .exists(unit.pixelDistanceFast(_) < With.configuration.battleMarginPixels)
  }
}
