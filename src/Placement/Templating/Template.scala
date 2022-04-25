package Placement.Templating

import Mathematics.Maff
import Mathematics.Points._
import Performance.{Cache, CacheForever}
import Placement.Access.PlaceLabels._
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

  def add(strings: String*): Template = {
    var x: Int = 0
    var y: Int = 0
    while (y < strings.length) {
      val string = strings(y)
      x = 0
      while (x < string.length) {
        val char = string(x)
        val pt: TemplatePointRequirement = char.toUpper match {
          case '-' => RequireWalkable
          case 'H' => new TemplatePointRequirement(4, 3).withLabels(TownHall)
          case 'G' => new TemplatePointRequirement(4, 3).withLabels(GroundProduction) // 4x3 ground production, eg. Gateway
          case 'R' => new TemplatePointRequirement(3, 2).withLabels(GroundProduction) // 3x2 ground production, eg. Robotics
          case 'T' => new TemplatePointRequirement(3, 2).withLabels(Tech) // 3x2 tech
          case 'P' => new TemplatePointRequirement(Protoss.Pylon).withLabels(PriorityPower, Supply)
          case 'C' => new TemplatePointRequirement(Terran.MissileTurret, Protoss.PhotonCannon, Zerg.CreepColony).withLabels(Defensive)
          case 'B' => new TemplatePointRequirement(Terran.Bunker, Protoss.ShieldBattery).withLabels(Defensive)
          case '6' => new TemplatePointRequirement(6, 3)
          case '4' => new TemplatePointRequirement(4, 3)
          case '3' => new TemplatePointRequirement(3, 2)
          case '2' => new TemplatePointRequirement(2, 2)
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
