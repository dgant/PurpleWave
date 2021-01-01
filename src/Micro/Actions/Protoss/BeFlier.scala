package Micro.Actions.Protoss

import Debugging.Visualizations.Forces
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.FewShot
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object BeFlier extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.airborne && unit.canMove && ! unit.unitClass.isBuilding

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val scourge = unit.matchups.threats
      .filter(t =>
        t.is(Zerg.Scourge)
        && t.pixelDistanceEdge(unit) < Math.max(96, if (unit.canAttackAir) 32 + unit.pixelRangeAir else 9))
      .toVector

    if (scourge.nonEmpty) {

      if ( ! unit.is(Terran.Valkyrie) && (unit.unitClass.dealsRadialSplashDamage || scourge.length == 1)) {
        // May not actually be implemented yet
        FewShot(unit, Zerg.Scourge, 1)
      }

      // TODO: Sniping scourge that aren't facing us
      // TODO: Triangle juking of scourge

      val safety = ByOption.minBy(With.units.ours.view.filter(u => u.canAttackAir && ! u.flying))(_.pixelDistanceEdge(unit))
      unit.agent.forces(Forces.threat) = Potential.unitAttraction(unit, scourge.minBy(_.pixelDistanceEdge(unit)), -1)
      safety.foreach(s => unit.agent.forces(Forces.regrouping) = Potential.unitAttraction(unit, s, 1))
      val waypoint = MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians)
      if (waypoint.isDefined) {
        unit.agent.toTravel = waypoint
        With.commander.move(unit)
      }
    }
  }
}
