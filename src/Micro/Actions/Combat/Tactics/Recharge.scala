package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Recharge extends Action {
  
  // Note: Recharge Shields range is 2

  lazy val priorityClasses = Seq(Protoss.Reaver, Protoss.Shuttle, Protoss.Carrier, Protoss.Arbiter, Protoss.Scout, Protoss.Corsair)
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.self.isProtoss
    && unit.canMove
    && unit.shieldPoints < unit.unitClass.maxShields
    && With.units.existsOurs(Protoss.ShieldBattery)
    && unit.pixel.metro.exists(m => unit.agent.destinationFinal.pixel.forall(_.metro.contains(m)))
    && (unit.isAny(priorityClasses: _*) || ! priorityClasses.exists(c => With.units.existsOurs(c))))
  
  protected def validBattery(unit: UnitInfo): Boolean = (
    unit.isOurs
    && unit.complete
    && Protoss.ShieldBattery(unit)
    && unit.energy > 0)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val missingShields = unit.unitClass.maxShields - unit.shieldPoints
    val batteries = unit.metro.toSeq.flatMap(_.ourUnits.filter(validBattery)).filter(b =>
      b.energy * 2 >= missingShields
      || ! unit.flying && b.pixelDistanceEdge(unit) < 64)

    if (batteries.isEmpty) return

    val battery = batteries.minBy(unit.pixelDistanceEdge)
    val distance = battery.pixelDistanceEdge(unit)

    if (unit.flying) {
      if (With.units.enemy.forall(e => distance < unit.pixelDistanceEdge(e))) {
        Commander.rightClick(unit, battery)
      }
      return
    }

    if ( ! (unit.totalHealth < unit.unitClass.maxTotalHealth / 3.0 || ! unit.agent.shouldFight || unit.matchups.pixelsToTargetRange.forall(_ > 320))) return
    if (unit.readyForAttackOrder && unit.matchups.targetsInRange.nonEmpty) return // Particularly to ensure that ramp-holders don't get stuck trying to get to a battery

    unit.agent.decision.set(battery.pixel.project(unit.pixel, 40))
  
    // Don't right click it from too far -- that gets you killed en route.
    lazy val safeToRecharge       = unit.matchups.threats.forall(threat => distance < Math.max(threat.pixelDistanceEdge(battery), unit.pixelDistanceEdge(threat) + threat.pixelRangeAgainst(unit)))
    lazy val badlyNeedToRecharge  = unit.shieldPoints * 3 < unit.unitClass.maxShields
    if (distance < 96 || (safeToRecharge && badlyNeedToRecharge)) {
      Commander.rightClick(unit, battery)
    }

    Potshot.delegate(unit)
    Commander.move(unit)
  }
}
