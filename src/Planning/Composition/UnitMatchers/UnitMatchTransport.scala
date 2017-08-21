package Planning.Composition.UnitMatchers

import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchTransport extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean =
    unit.aliveAndComplete             &&
    unit.flying                       &&
    unit.unitClass.spaceProvided > 0  &&
    ( ! unit.is(Zerg.Overlord) || unit.player.hasUpgrade(Zerg.OverlordDrops))
}
