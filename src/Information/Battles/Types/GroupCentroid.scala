package Information.Battles.Types

import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

object GroupCentroid {
  def air(units: Iterable[UnitInfo], extract: UnitInfo => Pixel): Pixel = Maff.centroid(units.view.map(extract))
  def ground(units: Iterable[UnitInfo], extract: UnitInfo => Pixel): Pixel = {
    val groundedUnits = Maff.orElse(units.view.filterNot(_.airborne), units.view.filterNot(_.flying))
    if (groundedUnits.isEmpty) return air(units, extract).walkablePixel
    val center = Maff.centroid(groundedUnits.map(extract))
    groundedUnits.map(extract).minBy(_.pixelDistanceSquared(center)).walkablePixel
  }
}
