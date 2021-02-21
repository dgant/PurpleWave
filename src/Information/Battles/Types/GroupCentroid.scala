package Information.Battles.Types

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

object GroupCentroid {
  def air(units: Iterable[UnitInfo]): Pixel = PurpleMath.centroid(units.view.map(_.pixel))
  def ground(units: Iterable[UnitInfo]): Pixel = {
    val nonFliers = units.view.filterNot(_.flying)
    if (nonFliers.isEmpty) return air(units).nearestWalkablePixel
    val center = PurpleMath.centroid(nonFliers.map(_.pixel))
    nonFliers.map(_.pixel).minBy(_.pixelDistanceSquared(center))
  }
}
