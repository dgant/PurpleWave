package Placement

import Mathematics.Points.{Point, Tile, TileRectangle}
import Performance.{Cache, CacheForever}
import ProxyBwapi.Races.Protoss
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class PreplacementTemplate {

  val points: ArrayBuffer[PreplacementPoint] = new ArrayBuffer[PreplacementPoint]()

  val left    : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.x)).getOrElse(0))
  val right   : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.x + p.slot.width)).getOrElse(0))
  val top     : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.y)).getOrElse(0))
  val bottom  : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.y + p.slot.height)).getOrElse(0))
  val start   : Cache[Tile] = new CacheForever(() => Tile(top(), left()))
  val end     : Cache[Tile] = new CacheForever(() => Tile(bottom(), right()))
  val area    : Cache[TileRectangle] = new CacheForever[TileRectangle](() => TileRectangle(start(), end()))
  def width   : Int         = right() - left()
  val height  : Int         = bottom() - top()

  def add(point: Point, pt: PreplacementType): PreplacementTemplate = {
    points += PreplacementPoint(point, pt)
    left.invalidate()
    right.invalidate()
    top.invalidate()
    bottom.invalidate()
    start.invalidate()
    end.invalidate()
    area.invalidate()
    return this
  }

  def add(string: String): PreplacementTemplate = {
    var x: Int = 0
    var y: Int = 0
    var onNewline: Boolean = true
    def point(): Point = Point(x, y)
    string.foreach(char => {
      if (char.isWhitespace) {
        if ( ! onNewline) {
          onNewline = true
          x = 0
          y += 1
        }
      } else {
        onNewline = false
        val pt: PreplacementType = char.toLower match {
          case '-' => ReservedWalkable
          case 'p' => new PreplacementType(Protoss.Pylon)
          case 'g' => new PreplacementType(Protoss.Gateway)
          case 'f' => new PreplacementType(Protoss.Forge)
          case 'y' => new PreplacementType(Protoss.CyberneticsCore)
          case 'c' => new PreplacementType(Protoss.PhotonCannon)
          case 'b' => new PreplacementType(Protoss.ShieldBattery)
          case '4' => new PreplacementType(4, 3)
          case '3' => new PreplacementType(3, 2)
          case '2' => new PreplacementType(2, 2)
          case default => Unreserved
        }
        add(point(), pt)
      }
    })
    return this
  }
}
