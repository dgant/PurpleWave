package Information.Geography.Types

import Debugging.RadianArrow
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Direction, Pixel, Points, Tile, TileRectangle}
import Performance.Cache
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

trait Geo {
  val tiles           : Set[Tile]
  def heart           : Tile
  def owner           : PlayerInfo
  def units           : Seq[UnitInfo]
  def bases           : Vector[Base]
  def zones           : Vector[Zone]

  lazy val isStartLocation  : Boolean           = bases.map(_.townHallTile).exists(With.geography.startLocations.contains)
  lazy val isCross          : Boolean           = ! With.geography.startBases.sortBy(With.geography.ourMain.groundDistance).exists(bases.contains)
  lazy val island           : Boolean           = With.geography.startBases.map(_.heart).count(With.paths.groundPathExists(_, centroid)) < 2
  lazy val isInterior       : Boolean           = zones.forall(z => z.metro.exists(m => ! m.exits.exists(_.zones.contains(z))))
  lazy val isBackyard       : Boolean           = zones.forall(z => z.metro.exists(m => ! m.exits.exists(_.zones.contains(z)) && z.rushDistanceMin > m.rushDistanceMin))
  lazy val isPocket         : Boolean             = isInterior && ! isStartLocation && ! island
  lazy val radians          : Double            = Points.middle.radiansTo(heart.center)
  lazy val rushDistanceMin  : Double            = Maff.max(rushDistances).getOrElse(0)
  lazy val rushDistanceMax  : Double            = Maff.max(rushDistances).getOrElse(0)
  lazy val arrow            : String            = RadianArrow(Points.tileMiddle.radiansTo(heart))
  lazy val adjective        : String            = if (island) "island " else if (isBackyard) "backyard " else if (isPocket) "pocket " else ""
  lazy val centroid         : Tile              = Maff.centroidTiles(Maff.orElse(tiles.filter(_.walkableUnchecked), tiles))
  lazy val boundary         : TileRectangle     = new TileRectangle(tiles)
  lazy val border           : Set[Tile]         = tiles.filter( ! _.adjacent8.forall(tiles.contains))
  lazy val selfAndChildren  : Vector[Geo]       = (Vector[Geo](this) ++ zones ++ bases).distinct
  lazy val edges            : Vector[Edge]      = With.geography.edges.filter(_.zones.exists(zones.contains))
  lazy val exits            : Vector[Edge]      = edges.filter(_.zones.exists( ! zones.contains(_))).distinct
  lazy val rushDistances    : Vector[Double]    = With.geography.metros.filter(_.isStartLocation).filterNot(_.selfAndChildren.contains(this)).map(groundDistance)
  lazy val entranceOriginal : Option[Edge]      = _entranceCache()
  lazy val exitOriginal     : Option[Edge]      = Maff.minBy(exits)(e => With.geography.startBases.map(_.heart).map(e.distanceGrid.get).max)
  lazy val exitDirection    : Option[Direction] = exitOriginal.map(_.pixelCenter.subtract(heart.center).direction)

  lazy val groundPixelsToBases: Map[Base, Double] = With.geography.bases.map(b => (b, b.heart.groundPixels(heart))).toMap
  lazy val groundPixelsToZones: Map[Zone, Double] = With.geography.zones.map(z => (z, z.heart.groundPixels(heart))).toMap

  def isOurs        : Boolean       = owner.isUs
  def isAlly        : Boolean       = owner.isAlly
  def isEnemy       : Boolean       = owner.isEnemy
  def isNeutral     : Boolean       = owner.isNeutral
  def ourUnits      : Seq[UnitInfo] = units.view.filter(_.isOurs)
  def allies        : Seq[UnitInfo] = units.view.filter(_.isFriendly)
  def enemies       : Seq[UnitInfo] = units.view.filter(_.isEnemy)
  def entranceNow   : Option[Edge]  = _entranceCache()
  def exitNow       : Option[Edge]  = _exitCache()

  def airDistance     (other: Geo)  : Double        = heart.pixelDistance(other.heart)
  def groundDistance  (other: Geo)  : Double        = heart.groundPixelsBidirectional(other.heart)
  def edgeTo          (to: Pixel)   : Option[Edge]  = Maff.minBy(exits)(_.pixelCenter.groundPixels(to))

  private val _entranceCache  = new Cache(() => Maff.minBy(exits)(edge => With.geography.startLocations.map(edge.distanceGrid.get).min))
  private val _exitCache      = new Cache(() => {
    val enemyZone = bases.exists(_.isEnemy) && ! bases.exists(_.isOurs)
    // Take the edge closest to opposing production
    Maff.minBy(edges)(edge =>
      Maff.orElse(
        ?(enemyZone, Seq(With.geography.ourMain.heart), With.scouting.enemyMain.map(_.heart).toSeq),
        ?(enemyZone, Seq(With.scouting.ourThreatOrigin), Seq(With.scouting.enemyThreatOrigin)))
        .map(edge.distanceGrid.get))
  })
}
