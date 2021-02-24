package Micro.Squads.Recruitment

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ProximityValue {
  def apply(candidate: FriendlyUnitInfo, destinations: Seq[Pixel]): Double = {
    // We want to reward being close but not let the multiplicative factor blow up as the unit gets very close.
    // After some experimentation with graphing it, I like 1 / sqrt(1 + tiles/8)
    ByOption.max(destinations.map(d => 1.0 / Math.sqrt(1.0 + candidate.pixelDistanceTravelling(d) / 8 / 32))).getOrElse(1.0)
  }
}
