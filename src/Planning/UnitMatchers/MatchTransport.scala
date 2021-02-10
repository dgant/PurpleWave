package Planning.UnitMatchers

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object MatchTransport extends Matcher {
  
  override def apply(unit: UnitInfo): Boolean =
    unit.aliveAndComplete             &&
    unit.flying                       &&
    unit.unitClass.spaceProvided > 0  &&
    ( ! unit.is(Zerg.Overlord) || unit.player.hasUpgrade(Zerg.OverlordDrops))
}
