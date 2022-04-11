package Placement.Templating

import Mathematics.Maff
import Mathematics.Points._
import Performance.{Cache, CacheForever}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Template {

  def this(units: Seq[(Tile, UnitClass)]) {
    this()
    val xMin = Maff.min(units.view.map(_._1.x)).getOrElse(0)
    val yMin = Maff.min(units.view.map(_._1.y)).getOrElse(0)
    units.foreach(tileUnit => add(
      Point(
        tileUnit._1.x - xMin,
        tileUnit._1.y - yMin),
      new TemplatePointRequirement(tileUnit._2)))
  }

  val points  : ArrayBuffer[TemplatePoint] = new ArrayBuffer
  val left    : Cache[Int]  = new CacheForever(() => Maff.min(points.view.map(_.point.x)).getOrElse(0))
  val right   : Cache[Int]  = new CacheForever(() => Maff.max(points.view.map(p => p.point.x + p.requirement.width)).getOrElse(0))
  val top     : Cache[Int]  = new CacheForever(() => Maff.min(points.view.map(_.point.y)).getOrElse(0))
  val bottom  : Cache[Int]  = new CacheForever(() => Maff.max(points.view.map(p => p.point.y + p.requirement.height)).getOrElse(0))
  val start   : Cache[Tile] = new CacheForever(() => Tile(top(), left()))
  val end     : Cache[Tile] = new CacheForever(() => Tile(bottom(), right()))
  val area    : Cache[TileRectangle] = new CacheForever[TileRectangle](() => TileRectangle(start(), end()))
  def width   : Int         = right() - left()
  def height  : Int         = bottom() - top()

  def add(point: Point, requirement: TemplatePointRequirement): Template = {
    points += TemplatePoint(point, requirement)
    left.invalidate()
    right.invalidate()
    top.invalidate()
    bottom.invalidate()
    start.invalidate()
    end.invalidate()
    area.invalidate()
    this
  }

  def add(string: String): Template = {
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
        val pt: TemplatePointRequirement = char.toLower match {
          case '-' => RequireWalkable
          case 't' => new TemplatePointRequirement(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery)
          case 'h' => new TemplatePointRequirement(Terran.Armory, Terran.Academy, Protoss.Forge, Protoss.CyberneticsCore, Protoss.CitadelOfAdun, Protoss.TemplarArchives, Protoss.RoboticsSupportBay, Protoss.Observatory, Protoss.ArbiterTribunal, Protoss.FleetBeacon, Zerg.EvolutionChamber, Zerg.SpawningPool, Zerg.HydraliskDen, Zerg.QueensNest, Zerg.UltraliskCavern) // 3x2 tech
          case 'p' => new TemplatePointRequirement(Protoss.Pylon)
          case 'g' => new TemplatePointRequirement(Terran.Barracks, Protoss.Gateway, Protoss.Stargate) // 4x3 production
          case 'r' => new TemplatePointRequirement(Protoss.RoboticsFacility) // 3x2 production
          case 'f' => new TemplatePointRequirement(Protoss.Forge)
          case 'y' => new TemplatePointRequirement(Protoss.CyberneticsCore)
          case 'c' => new TemplatePointRequirement(Protoss.PhotonCannon)
          case 'b' => new TemplatePointRequirement(Protoss.ShieldBattery)
          case '6' => new TemplatePointRequirement(6, 3)
          case '4' => new TemplatePointRequirement(4, 3)
          case '3' => new TemplatePointRequirement(3, 2)
          case '2' => new TemplatePointRequirement(2, 2)
          case default => RequireAnything
        }
        if (pt != RequireAnything) {
          add(point(), pt)
        }
        x += 1
      }
    })
    this
  }

  override def toString: String = f"PreplacementTemplate $width x $height"
}
