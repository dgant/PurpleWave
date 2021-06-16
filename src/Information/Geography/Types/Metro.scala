package Information.Geography.Types

import Lifecycle.With
import Mathematics.Points.Tile

case class Metro(bases: Seq[Base]) {
  def merge(other: Metro): Metro = Metro(bases ++ other.bases)
  val main: Option[Base] = bases.find(_.isStartLocation)
  val natural: Option[Base] = main.flatMap(_.natural)
  val zones: Vector[Zone] = With.geography.zones.filter(z =>
    z.bases.exists(bases.contains)
    || (z.bases.isEmpty && {
      val center = z.centroid.nearestWalkableTile
      With.paths.aStar(center, With.geography.startLocations.maxBy(_.groundTilesManhattan(center))).tiles.exists(_.exists(_.base.exists(bases.contains)))
    })
  )
  val tiles: Set[Tile] = zones.flatMap(_.tiles.view).toSet
}
