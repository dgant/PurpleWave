package Micro.Actions.Combat.Tactics

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && ! unit.canAttack
    && ! unit.agent.canFocus
    && (unit.matchups.framesOfSafety > GameTime(0, 2)() || unit.totalHealth > 500)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val goal: Option[Pixel] = destinationFromTeammates(unit, unit.squadmates)
      .orElse(destinationFromTeammates(unit, unit.battle.map(_.teamOf(unit).units).getOrElse(Iterable.empty)))

    if (goal.isDefined) {
      unit.agent.toTravel = goal
      Move.delegate(unit)
    }
  }

  protected def destinationFromTeammates(unit: FriendlyUnitInfo, teammates: Iterable[UnitInfo]): Option[Pixel] = {
    ByOption.minBy(unit.squadmates.filter(_.canAttack).map(_.pixelCenter))(_.pixelDistance(unit.agent.destination))
  }
}
