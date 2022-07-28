package Placement.Templating

import Mathematics.Maff
import Mathematics.Points._
import Performance.{Cache, CacheForever}
import Placement.Access.PlaceLabels._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

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

  def from(strings: String*): Template = {
    ascii = Some(strings.mkString("\n"))
    var x: Int = 0
    var y: Int = 0
    while (y < strings.length) {
      val string = strings(y)
      x = 0
      while (x < string.length) {
        val char = string(x)
        val pt: TemplatePointRequirement = char.toUpper match {
          case '-' => new RequireWalkable
          case 'A' => new RequireWalkable
          case 'B' => new TemplatePointRequirement(Terran.Bunker, Protoss.ShieldBattery).withLabels(Defensive)
          case 'C' => new TemplatePointRequirement(Terran.MissileTurret, Protoss.PhotonCannon, Zerg.CreepColony).withLabels(Defensive)
          case 'D' => last.labels ++= Seq(Defensive, DefendHall); null
          case 'E' => last.labels ++= Seq(Defensive, DefendEntrance); null
          case 'F' => new TemplatePointRequirement(Terran.Factory).withLabels(GroundProduction)
          case 'G' => new TemplatePointRequirement(4, 2).withLabels(Gas)
          case 'H' => new TemplatePointRequirement(4, 3).withLabels(TownHall)
          case 'I' => last.labels += Important; null
          case 'J' => null
          case 'K' => null
          case 'L' => null
          case 'M' => last.labels ++= Seq(Defensive, DefendHall, DefendAir); null
          case 'N' => last.labels ++= Seq(Defensive, DefendHall, DefendGround); null
          case 'O' => null
          case 'P' => new TemplatePointRequirement(Protoss.Pylon).withLabels(Important, Supply)
          case 'Q' => null
          case 'R' => new TemplatePointRequirement(3, 2).withLabels(GroundProduction) // 3x2 ground production, eg. Robotics
          case 'S' => new TemplatePointRequirement(Protoss.Stargate).withLabels(Tech)
          case 'T' => new TemplatePointRequirement(3, 2).withLabels(Tech) // 3x2 tech
          case 'U' => last.labels += Unimportant; null
          case 'V' => null
          case 'W' => new TemplatePointRequirement(4, 3).withLabels(GroundProduction) // 4x3 ground production, eg. Gateway
          case 'X' => null
          case 'Y' => null
          case 'Z' => null
          case '0' => null
          case '1' => null
          case '2' => new TemplatePointRequirement(2, 2)
          case '3' => new TemplatePointRequirement(3, 2)
          case '4' => new TemplatePointRequirement(4, 3)
          case '5' => null
          case '6' => new TemplatePointRequirement(Terran.Starport, Terran.ScienceFacility).withLabels(Tech)
          case '7' => null
          case '8' => null
          case '9' => null
          case default => null
        }
        if (pt != null) {
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

  def addLabels(placeLabels: PlaceLabel*): Template = {
    points.foreach(_.requirement.labels ++= placeLabels)
    this
  }

  private def last = points.last.requirement

  override def toString: String = f"PreplacementTemplate $width x $height"
}
