package Utilities.UnitFilters

import Debugging.SimpleString
import ProxyBwapi.UnitInfo.UnitInfo

trait UnitFilter extends Function[UnitInfo, Boolean] with SimpleString
