package Information.Battles.Types

import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

object GroupCentroid {
  def air(units: Iterable[UnitInfo]): Pixel = Maff.centroid(units.view.map(_.pixel))
  def ground(units: Iterable[UnitInfo]): Pixel = {
    var groundedUnits = Maff.orElse(units.view.filterNot(_.airborne), units.view.filterNot(_.flying))
    if (groundedUnits.isEmpty) {
      return air(units).walkablePixel
    }
    val center = Maff.centroid(groundedUnits.map(_.pixel))
    groundedUnits.map(_.pixel).minBy(_.pixelDistanceSquared(center))
  }
}
