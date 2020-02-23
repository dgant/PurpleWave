package Micro.Heuristics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRectangle}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class AOETarget(
    target: UnitInfo,
    caster: FriendlyUnitInfo,
    mx: Int,
    my: Int,
    pixelWidth: Int,
    pixelHeight: Int,
    projectionFrames: Double,
    evaluate: (UnitInfo, FriendlyUnitInfo) => Double) {
  val margin = 8
  private val p = target.pixelCenter
  private val xs = Seq(target.x - mx * margin, target.x + mx * (pixelWidth - margin))
  private val ys = Seq(target.y - my * margin, target.y + my * (pixelHeight - margin))
  private val evalPixelStart = Pixel(xs.min, ys.min)
  private val evalPixelEnd = Pixel(xs.max, ys.max)
  val rectangle = PixelRectangle(evalPixelStart, evalPixelEnd)
  val units: Set[UnitInfo] = rectangle
    .pixelsEach32
    .view
    .map(_.tileIncluding)
    .filter(_.valid)
    .flatMap(With.grids.units.get(_).filter(u => u.likelyStillThere && rectangle.contains(u.pixelCenter)))
    .toSet
  val netValue: Double = units.view.map(evaluate(_, caster)).sum
  lazy val positionsProjected = units.map(_.projectFrames(projectionFrames))
  lazy val xMin: Int = ByOption.min(positionsProjected.map(_.x)).getOrElse(p.x)
  lazy val yMin: Int = ByOption.min(positionsProjected.map(_.y)).getOrElse(p.y)
  lazy val xMax: Int = ByOption.max(positionsProjected.map(_.x)).getOrElse(p.x)
  lazy val yMax: Int = ByOption.max(positionsProjected.map(_.y)).getOrElse(p.y)
  lazy val finalTarget = Pixel((xMin + xMax) / 2, (yMin + yMax) / 2)

  def drawMap(): Unit = {
    val colorBright = Colors.NeonTeal
    val colorDark = Colors.DarkTeal
    DrawMap.circle(target.pixelCenter, target.unitClass.dimensionMin + 2, Colors.MediumRed)
    DrawMap.box(Pixel(xMin, yMin), Pixel(xMax, yMax), colorBright)
    DrawMap.box(evalPixelStart, evalPixelEnd, colorDark)
    DrawMap.drawStar(finalTarget, 7, colorBright, solid = true)
    units.foreach(unit => {
      DrawMap.circle(unit.pixelCenter, unit.unitClass.dimensionMin, colorBright)
      DrawMap.label(evaluate(unit, caster).toInt.toString, unit.pixelCenter.add(0, unit.unitClass.dimensionDown + 8))
    })
    DrawMap.label(netValue.toInt.toString, Pixel(finalTarget.x, yMax + 8), backgroundColor = colorDark, drawBackground = true)
  }
}