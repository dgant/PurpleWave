package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Recharge extends Action {
  
  // Note: Recharge Shields range is 2
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && (unit.agent.toReturn.isEmpty ||unit.readyForAttackOrder || unit.matchups.targetsInRange.isEmpty) // Particularly to ensure that ramp-holders don't get stuck trying to get to a battery
    && unit.shieldPoints < unit.unitClass.maxShields / 3
    && (unit.totalHealth < unit.unitClass.maxTotalHealth / 3.0 || ! unit.agent.shouldEngage)
  )
  
  protected def validBattery(unit: UnitInfo): Boolean = (
    unit.isOurs
    && unit.aliveAndComplete
    && unit.is(Protoss.ShieldBattery)
    && unit.energy > 20 // 2 shield per energy
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    var batteries = unit.matchups.allies.filter(validBattery)
    if (batteries.isEmpty) batteries = unit.zone.units.filter(validBattery)

    val battery = ByOption.minBy(batteries)(_.pixelDistanceEdge(unit))
    if (battery.isEmpty) return
    
    unit.agent.toTravel = battery.map(_.pixel)
  
    // Shield battery range is 2 tiles.
    // Don't right click it from too far -- that gets you killed en route.
    val inRangeToRecharge = unit.pixelDistanceEdge(battery.get) < 32.0 * 3.0
    val safeToRecharge = unit.matchups.threats.forall(threat => {
      val distanceMeThreat      = unit.pixelDistanceEdge(threat) + threat.pixelRangeAgainst(unit)
      val distanceMeBattery     = unit.pixelDistanceEdge(battery.get)
      val distanceThreatBattery = threat.pixelDistanceEdge(battery.get)
      val safe =
        distanceMeBattery < distanceThreatBattery ||
        distanceMeBattery < distanceMeThreat
      safe
    })
    if (inRangeToRecharge || safeToRecharge) {
      battery.foreach(With.commander.rightClick(unit, _))
    }
  }
}
