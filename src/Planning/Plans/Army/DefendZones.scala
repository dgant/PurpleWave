package Planning.Plans.Army

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Planning.Composition.Property
import Planning.Composition.UnitMatchers.{UnitMatchWarriors, UnitMatcher}
import Planning.Plan
import ProxyBwapi.UnitInfo.ForeignUnitInfo

class DefendZones extends Plan {
  
  private lazy val zones = With.geography.zones.map(zone => (zone, new DefendZone(zone))).toMap
  
  val defenderMatcher = new Property[UnitMatcher](UnitMatchWarriors)
  
  override def getChildren: Iterable[Plan] = zones.values
  
  protected override def onUpdate() {
    val zoneScores = zones
      .keys
      .map(zone => (zone, zoneValue(zone)))
      .filter(_._2 > 0.0)
      .toMap
    
    if (zoneScores.isEmpty) {
      // Safety valve for a weird situation in which we have no bases.
      return
    }
    
    val zoneByEnemyUnfiltered = With.units.enemy
      .filter(_.likelyStillThere)
      .map(enemy => (enemy, zoneScores.keys.minBy(zone => enemy.pixelDistanceTravelling(zone.centroid))))
      .toMap
    
    val zoneByEnemyFiltered = zoneByEnemyUnfiltered.filter(pair => isThreatening(pair._1, pair._2))
    
    zoneScores
      .toVector
      .sortBy(-_._2)
      .map(_._1)
      .foreach(zone => {
        val plan = zones(zone)
        plan.enemies = zoneByEnemyFiltered.filter(_._2 == zone).keys.toSeq
        delegate(plan)
      })
  }
  
  private def zoneValue(zone: Zone): Double = {
    zone.bases.map(baseValue).sum + zone.units.filter(u => u.unitClass.isBuilding && u.isOurs).map(_.subjectiveValue).sum
  }
  
  private def baseValue(base: Base): Double = {
    (5.0 + base.workers.size) * (if (base.owner.isFriendly) 1.0 else 0.0)
  }
  
  private def isThreatening(enemy: ForeignUnitInfo, zone: Zone): Boolean = {
    (
      enemy.likelyStillAlive
      && (enemy.unitClass.helpsInCombat || enemy.isTransport)
      && enemy.zone == zone
    )
  }
}
