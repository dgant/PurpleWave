package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object ShuttleRegroup extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    false && // Disabled because saw a case where supportDeepest == supportSafest
    unit.is(Protoss.Shuttle) && unit.agent.passengers.forall(_.loaded))

  // Don't go too crazy ferrying charges around

  private def isAntiAir(unit: FriendlyUnitInfo, threat: UnitInfo): Boolean = {
    threat.isEnemy && threat.canMove && threat.likelyStillThere && threat.canAttack(unit)
  }
  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    // Who can shoot us down?
    val antiAir = (
        unit.matchups.threats.view
        ++ unit.agent.passengers.flatMap(_.agent.destination.zone.units.view))
      .filter(isAntiAir(unit, _))
      .distinct
      .toVector

    if (antiAir.nonEmpty) {

      // Help!
      val support = unit.alliesBattleThenSquad.flatten.filter(u => u.canAttack && ! u.isAny(Protoss.Reaver, MatchWorker)).toVector
      if (support.nonEmpty) {
        val supportDeepest = support.maxBy(_.matchups.pixelsOfEntanglement)
        val supportSafest = support.maxBy(_.matchups.pixelsOfEntanglement)
        var formation = supportDeepest.pixel.project(
          supportSafest.pixel,
          if (supportDeepest.matchups.threats.isEmpty)
            32.0 * 7.0 - supportDeepest.effectiveRangePixels
          else
            32.0 * 10.0 - supportDeepest.effectiveRangePixels)
        formation = Spiral
          .points(8)
          .map(p => formation.add(32 * p.x, 32* p.y))
          .find(p => With.grids.walkable.get(p.tile))
          .getOrElse(formation)

        if (unit.pixelDistanceCenter(formation) > 32.0 * 12.0) {
          unit.agent.toTravel = Some(formation)
          unit.agent.passengers.foreach(_.agent.directRide(formation))
        }
      }
    }
  }
}
