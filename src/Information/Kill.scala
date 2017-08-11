package Information

import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

case class Kill(killer: UnitInfo, victim: UnitClass, frame: Int)