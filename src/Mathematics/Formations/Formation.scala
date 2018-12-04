package Mathematics.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

case class Formation(val placements: Map[UnitInfo, Pixel])
