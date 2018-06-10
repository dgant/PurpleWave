package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class GoalEscort extends GoalBasic {
  
  override def toString: String = "Escort to " + destination.zone
  
  var principal: Option[FriendlyUnitInfo] = None
  
  override protected def destination: Pixel =
    principal
      .map(thePrincipal =>
        ByOption
          .minBy(squad.enemies.filter(_.canAttack(thePrincipal)))(_.framesBeforeAttacking(thePrincipal))
          .map(_.pixelCenter)
          .getOrElse(thePrincipal.projectFrames(24 * 5)))
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {}
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {}
}
