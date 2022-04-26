package Placement.Templating

import Mathematics.Maff
import Mathematics.Points._
import Performance.{Cache, CacheForever}
import Placement.Access.PlaceLabels._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class Template extends TemplateFilter {
  val points  : ArrayBuffer[TemplatePoint]  = new ArrayBuffer
  val left    : Cache[Int]                  = new CacheForever(() => Maff.min(points.view.map(_.point.x)).getOrElse(0))
  val right   : Cache[Int]                  = new CacheForever(() => Maff.max(points.view.map(p => p.point.x + p.requirement.width)).getOrElse(0))
  val top     : Cache[Int]                  = new CacheForever(() => Maff.min(points.view.map(_.point.y)).getOrElse(0))
  val bottom  : Cache[Int]                  = new CacheForever(() => Maff.max(points.view.map(p => p.point.y + p.requirement.height)).getOrElse(0))
  val start   : Cache[Tile]                 = new CacheForever(() => Tile(top(), left()))
  val end     : Cache[Tile]                 = new CacheForever(() => Tile(bottom(), right()))
  val area    : Cache[TileRectangle]        = new CacheForever[TileRectangle](() => TileRectangle(start(), end()))
  var ascii   : Option[String]              = None
  def width   : Int                         = right() - left()
  def height  : Int                         = bottom() - top()

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

  def add(point: Point, requirement: TemplatePointRequirement): Template = {
    points += TemplatePoint(point, requirement)
    // Put Town Halls/Gas up front because those are the easiest points to reject when fitting
    Maff.sortStablyInPlaceBy(points)(p => if (p.requirement.isTownHall) 0 else if (p.requirement.isGas) 1 else 2)
    left.invalidate()
    right.invalidate()
    top.invalidate()
    bottom.invalidate()
    start.invalidate()
    end.invalidate()
    area.invalidate()
    this
  }

  def add(strings: String*): Template = {
    ascii = Some(strings.mkString("\n"))
    var x: Int = 0
    var y: Int = 0
    while (y < strings.length) {
      val string = strings(y)
      x = 0
      while (x < string.length) {
        val char = string(x)
        val pt: TemplatePointRequirement = char.toUpper match {
          case '-' => RequireWalkable
          case 'A' => new TemplatePointRequirement(4, 3).withLabels(Tech)             // 4x3 air tech, eg. Stargate
          case 'B' => new TemplatePointRequirement(Terran.Bunker, Protoss.ShieldBattery).withLabels(Defensive)
          case 'C' => new TemplatePointRequirement(Terran.MissileTurret, Protoss.PhotonCannon, Zerg.CreepColony).withLabels(Defensive)
          case 'D' => new TemplatePointRequirement(4, 3).withLabels(GroundProduction) // 4x3 ground production, eg. Gateway
          case 'E' => RequireAnything
          case 'F' => new TemplatePointRequirement(Terran.Factory).withLabels(GroundProduction)
          case 'G' => new TemplatePointRequirement(4, 2).withLabels(Gas)
          case 'H' => new TemplatePointRequirement(4, 3).withLabels(TownHall)
          case 'I' => RequireAnything
          case 'J' => RequireAnything
          case 'K' => RequireAnything
          case 'L' => RequireAnything
          case 'M' => RequireAnything
          case 'N' => RequireAnything
          case 'O' => RequireAnything
          case 'P' => new TemplatePointRequirement(Protoss.Pylon).withLabels(PriorityPower, Supply)
          case 'Q' => RequireAnything
          case 'R' => new TemplatePointRequirement(3, 2).withLabels(GroundProduction) // 3x2 ground production, eg. Robotics
          case 'S' => RequireAnything
          case 'T' => new TemplatePointRequirement(3, 2).withLabels(Tech) // 3x2 tech
          case 'U' => RequireAnything
          case 'V' => RequireAnything
          case 'W' => RequireAnything
          case 'X' => RequireAnything
          case 'Y' => RequireAnything
          case 'Z' => RequireAnything
          case '0' => RequireAnything
          case '1' => RequireAnything
          case '2' => new TemplatePointRequirement(2, 2)
          case '3' => new TemplatePointRequirement(3, 2)
          case '4' => new TemplatePointRequirement(4, 3)
          case '5' => RequireAnything
          case '6' => new TemplatePointRequirement(Terran.Starport, Terran.ScienceFacility).withLabels(Tech)
          case '7' => RequireAnything
          case '8' => RequireAnything
          case '9' => RequireAnything
          case default => RequireAnything
        }
        if (pt != RequireAnything) {
          add(Point(x, y), pt)
          // TODO: Add postfix modifiers here
          // -Optional fits
          // -Must-fill (no undersize occupants)
          // Maybe: high-pri
          // Maybe: banned contents
          // Maybe: 2 types of "One of these must be unwalkable"
        }
        x += 1
      }
      y += 1
    }
    this
  }

  override def toString: String = f"PreplacementTemplate $width x $height"
}
