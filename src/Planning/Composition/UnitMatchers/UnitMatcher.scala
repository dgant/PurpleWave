package Planning.Composition.UnitMatchers

import ProxyBwapi.UnitInfo.UnitInfo

trait UnitMatcher {
  def accept(unit: UnitInfo): Boolean
  def acceptAsPrerequisite(unit: UnitInfo): Boolean = accept(unit)
}
