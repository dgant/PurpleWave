package Micro.Actions.Combat.Tactics

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && ! unit.unitClass.isTransport
    && ! unit.canAttack
    && ( ! unit.agent.canFocus || unit.unitClass.isFlyingBuilding)
    && (unit.matchups.framesOfSafety > GameTime(0, 2)() || unit.totalHealth > 500)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val goal: Option[Pixel] = destinationFromTeammates(unit, unit.squadmates)
      .orElse(destinationFromTeammates(unit, unit.battle.map(_.teamOf(unit).units).getOrElse(Iterable.empty)))
      .map(p =>
        if (With.grids.friendlyVision.isSet(p.tileIncluding))
          ByOption.minBy(unit.squadenemies.view.filter(!_.visible))(_.pixelDistanceCenter(p)).orElse(
            ByOption.minBy(unit.squadenemies)(_.pixelDistanceCenter(p)).orElse(
              ByOption.minBy(unit.matchups.enemies.view.filter(!_.visible))(_.pixelDistanceCenter(p)).orElse(
                ByOption.minBy(unit.matchups.enemies)(_.pixelDistanceCenter(p)))))
              .map(_.pixelCenter)
              .getOrElse(p.project(SpecificPoints.middle, 2 * unit.sightRangePixels))
        else p)

    if (goal.isDefined) {
      unit.agent.toTravel = goal
      Move.delegate(unit)
    }
  }

  def bonusDistance: Double = {
    if (With.enemies.exists(_.isTerran))
      5.0 * 32.0
    else
      3.0 * 32.0
  }

  protected def destinationFromTeammates(unit: FriendlyUnitInfo, teammates: Iterable[UnitInfo]): Option[Pixel] = {
    ByOption.minBy(unit.squadmates.filter(_.canAttack).map(u => u.pixelCenter.project(u.agent.destination, bonusDistance)))(_.pixelDistance(unit.agent.destination))
  }
}
