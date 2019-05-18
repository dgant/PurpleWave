package Planning.Plans.Army

import Information.Geography.Types.{Base, Zone}
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchRecruitableForCombat, UnitMatcher}
import Planning.{Plan, Property}

class DefendZones extends Plan {
  
  private lazy val zones = With.geography.zones.map(zone => (zone, new DefendZone(zone))).toMap
  
  val defenderMatcher = new Property[UnitMatcher](UnitMatchRecruitableForCombat)
  
  override def getChildren: Iterable[Plan] = zones.values
  
  protected override def onUpdate() {

    zones.values.foreach(_.goal.unitMatcher = defenderMatcher.get)

    val zoneScores = zones
      .keys
      .filter(_.units.exists(u => u.unitClass.isBuilding && u.isOurs))
      .map(zone => (zone, zoneValue(zone)))
      .filter(_._2 > 0.0)
      .toMap
    
    if (zoneScores.isEmpty) return
    
    val zoneByEnemy = With.units.enemy
      .view
      .filter(e => e.likelyStillAlive && e.likelyStillThere && (e.unitClass.dealsDamage || e.unitClass.isDetector || e.unitClass.isTransport))
      .map(enemy => (enemy, zoneScores.minBy(z => enemy.pixelDistanceTravelling(z._1.centroid))._1))
      .filter(pair => pair._1.framesToTravelTo(pair._2.centroid.pixelCenter) < GameTime(0, 20)())
      .toMap
    
    zoneScores
      .toVector
      .sortBy(-_._2)
      .map(_._1)
      .foreach(zone => {
        val plan = zones(zone)
        plan.enemies = zoneByEnemy.filter(_._2 == zone).keys.toSeq
        delegate(plan)
      })
  }
  
  private def zoneValue(zone: Zone): Double = {
    zone.units.view.map(u => if (u.isEnemy) u.subjectiveValue else 0.0).sum
  }
  
  private def baseValue(base: Base): Double = {
    (5.0 + base.workerCount) * (if (With.geography.ourBasesAndSettlements.contains(base)) 1.0 else 0.0)
  }
}
