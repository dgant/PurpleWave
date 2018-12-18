package Planning.UnitMatchers

import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.UnitInfo.UnitInfo

trait UnitMatcher {
  def accept(unit: UnitInfo): Boolean
  def acceptAsPrerequisite(unit: UnitInfo): Boolean = accept(unit)

  def toUnitClass: UnitClass = UnitClasses.None
}
