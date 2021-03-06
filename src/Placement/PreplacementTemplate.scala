package Placement

import Mathematics.Points._
import Performance.{Cache, CacheForever}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class PreplacementTemplate {

  def this(units: Seq[(Tile, UnitClass)]) {
    this()
    val xMin = ByOption.min(units.view.map(_._1.x)).getOrElse(0)
    val yMin = ByOption.min(units.view.map(_._1.y)).getOrElse(0)
    units.foreach(tileUnit => add(
      Point(
        tileUnit._1.x - xMin,
        tileUnit._1.y - yMin),
      new PreplacementRequirement(tileUnit._2)))
  }

  val points: ArrayBuffer[PreplacementSlot] = new ArrayBuffer[PreplacementSlot]()

  val left    : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.x)).getOrElse(0))
  val right   : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.x + p.requirement.width)).getOrElse(0))
  val top     : Cache[Int]  = new CacheForever(() => ByOption.min(points.view.map(_.point.y)).getOrElse(0))
  val bottom  : Cache[Int]  = new CacheForever(() => ByOption.max(points.view.map(p => p.point.y + p.requirement.height)).getOrElse(0))
  val start   : Cache[Tile] = new CacheForever(() => Tile(top(), left()))
  val end     : Cache[Tile] = new CacheForever(() => Tile(bottom(), right()))
  val area    : Cache[TileRectangle] = new CacheForever[TileRectangle](() => TileRectangle(start(), end()))
  def width   : Int         = right() - left()
  def height  : Int         = bottom() - top()

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
    var onWhitespace: Boolean = true
    def point(): Point = Point(x, y)
    string.foreach(char => {
      if (char.isWhitespace) {
        if ( ! onWhitespace) {
          onWhitespace = true
          x = 0
          y += 1
        }
      } else {
        onWhitespace = false
        val pt: PreplacementRequirement = char.toLower match {
          case '-' => ReservedWalkable
          case 't' => new PreplacementRequirement(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery)
          case 'h' => new PreplacementRequirement(Terran.Armory, Terran.Academy, Protoss.Forge, Protoss.CyberneticsCore, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.RoboticsSupportBay, Protoss.Observatory, Protoss.ArbiterTribunal, Protoss.FleetBeacon, Zerg.EvolutionChamber, Zerg.SpawningPool, Zerg.HydraliskDen, Zerg.QueensNest, Zerg.UltraliskCavern) // 3x2 tech
          case 'p' => new PreplacementRequirement(Protoss.Pylon)
          case 'g' => new PreplacementRequirement(Terran.Barracks, Protoss.Gateway, Protoss.Stargate) // 4x3 production
          case 'r' => new PreplacementRequirement(Protoss.RoboticsFacility) // 3x2 production
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
