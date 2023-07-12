package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Recharge extends Action {
  
  // Note: Recharge Shields range is 2
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.shieldPoints < unit.unitClass.maxShields
    && With.units.existsOurs(Protoss.ShieldBattery)
    && (unit.totalHealth < unit.unitClass.maxTotalHealth / 3.0 || ! unit.agent.shouldFight)
    && ( ! unit.readyForAttackOrder || unit.matchups.targetsInRange.isEmpty) // Particularly to ensure that ramp-holders don't get stuck trying to get to a battery
  )
  
  protected def validBattery(unit: UnitInfo): Boolean = (
    unit.isOurs
    && unit.complete
    && Protoss.ShieldBattery(unit)
    && unit.energy > 20 // 2 shield per energy
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    var batteries: Iterable[UnitInfo] = unit.alliesBattle.filter(validBattery)
    if (batteries.isEmpty) batteries = unit.zone.units.filter(validBattery)

    val battery = Maff.minBy(batteries)(_.pixelDistanceEdge(unit))
    if (battery.isEmpty) return
    
    unit.agent.toTravel = battery.map(_.pixel.project(unit.pixel, 40))
  
    // Shield battery range is 2 tiles.
    // Don't right click it from too far -- that gets you killed en route.
    val inRangeToRecharge   = unit.pixelDistanceEdge(battery.get) < 32.0 * 3.0
    lazy val safeToRecharge = unit.matchups.threats.forall(threat => {
      val distanceMeThreat      = unit.pixelDistanceEdge(threat) + threat.pixelRangeAgainst(unit)
      val distanceMeBattery     = unit.pixelDistanceEdge(battery.get)
      val distanceThreatBattery = threat.pixelDistanceEdge(battery.get)
      val safe =
        distanceMeBattery < distanceThreatBattery ||
        distanceMeBattery < distanceMeThreat
      safe
    })

    lazy val badlyNeedToRecharge = unit.shieldPoints * 3 < unit.unitClass.maxShields
    if (inRangeToRecharge || safeToRecharge && (inRangeToRecharge || badlyNeedToRecharge)) {
      battery.foreach(Commander.rightClick(unit, _))
    }

    Potshot.delegate(unit)
    Commander.move(unit)
  }
}
