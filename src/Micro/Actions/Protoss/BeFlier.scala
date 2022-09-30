package Micro.Actions.Protoss

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.FewShot
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object BeFlier extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.airborne && unit.canMove && ! unit.unitClass.isBuilding

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val scourge = unit.matchups.threats
      .filter(Zerg.Scourge)
      .filter(_.pixelDistanceEdge(unit) < Math.max(96, if (unit.canAttackAir) 32 + unit.pixelRangeAir else 9))
      .toVector

    if (scourge.nonEmpty) {

      if ( ! Terran.Valkyrie(unit) && (unit.unitClass.dealsRadialSplashDamage || scourge.length == 1)) {
        // Not actually be implemented yet
        FewShot(unit, Zerg.Scourge, 1)
      }

      // TODO: Sniping scourge that aren't facing us
      // TODO: Triangle juking of scourge

      val safety = Maff.minBy(With.units.ours.view.filter(u => u.canAttackAir && ! u.flying))(_.pixelDistanceEdge(unit))
      unit.agent.forces(Forces.threat) = Potential.towardsUnit(unit, scourge.minBy(_.pixelDistanceEdge(unit)), -1)
      safety.foreach(s => unit.agent.forces(Forces.regrouping) = Potential.towardsUnit(unit, s, 1))
      val waypoint = MicroPathing.getWaypointInDirection(unit, unit.agent.forces.sum.radians)
      if (waypoint.isDefined) {
        unit.agent.toTravel = waypoint
        Commander.move(unit)
      }
    }
  }
}
