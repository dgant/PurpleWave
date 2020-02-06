package Planning.Plans.Army

import Information.Geography.Types.Zone
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchRecruitableForCombat, UnitMatcher}
import Planning.{Plan, Property}
import ProxyBwapi.UnitInfo.UnitInfo

class DefendZones extends Plan {
  
  private lazy val zones = With.geography.zones.map(zone => (zone, new DefendZone(zone))).toMap
  
  val defenderMatcher = new Property[UnitMatcher](UnitMatchRecruitableForCombat)
  
  override def getChildren: Iterable[Plan] = zones.values

  def indicatesDefendableZone(unit: UnitInfo): Boolean = {
    if ( ! unit.isOurs) return false
    if ( ! unit.unitClass.isBuilding) return false
    if (unit.flying && ! unit.unitClass.isTownHall) return false
    true
  }
  
  protected override def onUpdate() {

    zones.values.foreach(_.goal.unitMatcher = defenderMatcher.get)

    val zoneScores = zones.keys.filter(_.units.exists(indicatesDefendableZone)).map(z => (z, zoneValue(z))).toMap
    
    if (zoneScores.isEmpty) return
    
    val zoneByEnemy = With.units.enemy
      .view
      .filter(e => e.likelyStillAlive && e.likelyStillThere && (e.unitClass.dealsDamage || e.unitClass.isDetector || e.isTransport))
      .map(enemy => (enemy, zoneScores.minBy(z => enemy.pixelDistanceTravelling(z._1.centroid))._1))
      .filter(pair =>
        pair._1.framesToTravelTo(pair._2.centroid.pixelCenter) < GameTime(0, 12)()
        && pair._1.pixelDistanceTravelling(pair._2.centroid.pixelCenter) < 32 * 64)
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
    // Omit Pylons/Supply Depots, which may just be there to block expansions
    1.0 + zone.units.view.map(u => if ((u.unitClass.isWorker || u.unitClass.isBuilding) && u.unitClass.supplyProvided != 16) u.subjectiveValue else 0.0).sum
  }
}
