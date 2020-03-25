package Planning.UnitMatchers

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitInfo.UnitInfo

case class UnitMatchTeching(tech: Tech = null) extends UnitMatcher {
  override def apply(unit: UnitInfo): Boolean =
    unit.teching && (tech == null || unit.techProducing.contains(tech))
}
