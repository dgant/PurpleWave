package Placement

import Mathematics.Points.{Point, Tile, TileRectangle}
import Performance.{Cache, CacheForever}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class PreplacementTemplate {

  val points: ArrayBuffer[PreplacementSlot] = new ArrayBuffer[PreplacementSlot]()

  val left    : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.x)).getOrElse(0))
  val right   : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.x + p.requirement.width)).getOrElse(0))
  val top     : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.y)).getOrElse(0))
  val bottom  : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.y + p.requirement.height)).getOrElse(0))
  val start   : Cache[Tile] = new CacheForever(() => Tile(top(), left()))
  val end     : Cache[Tile] = new CacheForever(() => Tile(bottom(), right()))
  val area    : Cache[TileRectangle] = new CacheForever[TileRectangle](() => TileRectangle(start(), end()))
  def width   : Int         = right() - left()
  val height  : Int         = bottom() - top()

  def add(point: Point, requirement: PreplacementRequirement): PreplacementTemplate = {
    points += PreplacementSlot(point, requirement)
    left.invalidate()
    right.invalidate()
    top.invalidate()
    bottom.invalidate()
    start.invalidate()
    end.invalidate()
    area.invalidate()
    this
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
        val pt: PreplacementRequirement = char.toLower match {
          case '-' => ReservedWalkable
          case 't' => new PreplacementRequirement(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery)
          case 'p' => new PreplacementRequirement(Protoss.Pylon)
          case 'g' => new PreplacementRequirement(Protoss.Gateway)
          case 'f' => new PreplacementRequirement(Protoss.Forge)
          case 'y' => new PreplacementRequirement(Protoss.CyberneticsCore)
          case 'c' => new PreplacementRequirement(Protoss.PhotonCannon)
          case 'b' => new PreplacementRequirement(Protoss.ShieldBattery)
          case '4' => new PreplacementRequirement(4, 3)
          case '3' => new PreplacementRequirement(3, 2)
          case '2' => new PreplacementRequirement(2, 2)
          case default => Unreserved
        }
        if (pt != Unreserved) {
          add(point(), pt)
        }
        x += 1
      }
    })
    this
  }

  override def toString: String = "PreplacementTemplate " + width + "x" + height + " "
}
