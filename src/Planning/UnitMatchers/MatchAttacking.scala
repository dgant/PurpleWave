package Planning.UnitMatchers
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object MatchAttacking extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.pixelDistanceCenter(With.geography.home.pixelCenter) <
    unit.pixelDistanceCenter(With.scouting.mostBaselikeEnemyTile.pixelCenter)
}
