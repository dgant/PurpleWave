package Planning.Plans.Macro.Protoss

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Macro.Automatic.Pump
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.UnitClass

class BuildTowersAtBases(towersRequired: Int, towerClass: UnitClass = Protoss.PhotonCannon) extends Plan {

  override def onUpdate() {
    val bases = eligibleBases
    val zones = bases.map(_.zone).toSet.toArray

    if (zones.nonEmpty) {
      if (towerClass == Protoss.PhotonCannon) {
        With.scheduler.request(this, Get(Protoss.Forge))
      } else if (towerClass == Terran.MissileTurret) {
        With.scheduler.request(this, Get(Terran.EngineeringBay))
      }
      zones.foreach(towerZone)
    }
  }

  protected def eligibleBases: Iterable[Base] = {
    With.geography.ourBasesAndSettlements
  }

  private def towerZone(zone: Zone): Unit = {
    lazy val pylonsInZone = zone.units.filter(u => u.isOurs && u.is(Protoss.Pylon))
    lazy val towersInZone = zone.units.filter(u => u.isOurs && u.is(towerClass))
    lazy val towersToAdd = towersRequired - towersInZone.size

    val needPylons = towerClass.requiresPsi
    
    if (needPylons && pylonsInZone.isEmpty) {
      new Pump(Protoss.Pylon, maximumConcurrently = 1)
    }

    if ( ! needPylons || pylonsInZone.exists(_.aliveAndComplete)) {
      new Pump(towerClass, maximumConcurrently = towersToAdd).update()
    }
  }
}
