package Information.Geography.Types

import Mathematics.Maff
import Mathematics.Points.{Points, Tile, TileRectangle}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitInfo.UnitInfo

trait Geo {
  val tiles           : Set[Tile]
  def heart           : Tile
  def owner           : PlayerInfo
  def units           : Seq[UnitInfo]
  def bases           : Seq[Base]
  def zones           : Seq[Zone]
  def isStartLocation : Boolean

  lazy val boundary : TileRectangle = new TileRectangle(tiles)
  lazy val centroid : Tile          = Maff.centroidTiles(Maff.orElse(tiles.filter(_.walkableUnchecked), tiles))
  lazy val border   : Set[Tile]     = tiles.filter( ! _.adjacent8.forall(tiles.contains))
  lazy val radians  : Double        = Points.middle.radiansTo(heart.center)

  def isOurs        : Boolean       = owner.isUs
  def isAlly        : Boolean       = owner.isAlly
  def isEnemy       : Boolean       = owner.isEnemy
  def isNeutral     : Boolean       = owner.isNeutral
  def ourUnits      : Seq[UnitInfo] = units.view.filter(_.isOurs)
  def allies        : Seq[UnitInfo] = units.view.filter(_.isFriendly)
  def enemies       : Seq[UnitInfo] = units.view.filter(_.isEnemy)
}
