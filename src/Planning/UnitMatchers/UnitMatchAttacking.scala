package Planning.UnitMatchers
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchAttacking extends UnitMatcher {
  override def accept(unit: UnitInfo): Boolean =
    unit.pixelDistanceCenter(With.geography.home.pixelCenter) <
    unit.pixelDistanceCenter(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
}
