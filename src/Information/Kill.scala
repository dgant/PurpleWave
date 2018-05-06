package Information

import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class Kill(killer: UnitInfo, victim: UnitClass, frame: Int)