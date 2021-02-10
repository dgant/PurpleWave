package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.UnitMatchers.MatchWorkers
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.ForeignUnitInfo

object ScoutTracking {
  def enemyScouts: Iterable[ForeignUnitInfo] = With.units.enemy.filter(u =>
    u.isAny(Zerg.Overlord, MatchWorkers)
    && u.base.exists(basesToConsider.contains))

  def basesToConsider: Vector[Base] = (With.geography.ourBases :+ With.geography.ourNatural).distinct
}
