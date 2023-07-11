package Micro.Actions.Protoss.Shuttle

import Mathematics.Maff
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

object Shuttling {
  // How far is the pickup radius for a Shuttle?
  // It's "1" eg. 32 edge-to-edge pixels.
  // https://github.com/OpenBW/openbw/blob/master/bwgame.h#L5059
  val pickupRadiusEdge = 32

  def pickupRadiusCenter(other: UnitInfo): Int = {
    pickupRadiusEdge + Maff.div2(Protoss.Shuttle.dimensionMin + other.unitClass.dimensionMin)
  }
}
