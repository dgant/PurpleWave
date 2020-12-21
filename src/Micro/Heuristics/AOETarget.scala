package Micro.Heuristics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRectangle}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class AOETarget(
    target: UnitInfo,
    caster: FriendlyUnitInfo,
    mx: Int,
    my: Int,
    pixelWidth: Int,
    pixelHeight: Int,
    lookaheadPixels: Double,
    evaluate: (UnitInfo, FriendlyUnitInfo) => Double) {
  val margin = 8
  private val p = target.pixelCenter
  private val xs = Seq(target.x - mx * margin, target.x + mx * (pixelWidth - margin))
  private val ys = Seq(target.y - my * margin, target.y + my * (pixelHeight - margin))
  private val evalPixelStart = Pixel(xs.min, ys.min)
  private val evalPixelEnd = Pixel(xs.max, ys.max)
  val rectangle = PixelRectangle(evalPixelStart, evalPixelEnd)
  // Get units in the *expanded* rectangle to handle the potential out-of-dateness of the unit grid
  def units: Iterable[UnitInfo] = rectangle.expand(64, 64)
    .pixelsEach32
    .view
    .map(_.tileIncluding)
    .filter(_.valid)
    .flatMap(With.grids.units.get(_).view.filter(u =>
      u.likelyStillThere
      && rectangle.contains(u.pixelCenter))) // Require the unit to actually be there; don't trust the grid))
  val netValue: Double = units.view.map(evaluate(_, caster)).sum
  var xMin = Int.MaxValue
  var yMin = Int.MaxValue
  var xMax = Int.MinValue
  var yMax = Int.MinValue
  if (units.isEmpty) {
    xMin = evalPixelStart.x
    yMin = evalPixelStart.y
    xMax = evalPixelEnd.x
    yMax = evalPixelEnd.y
  }
  units.foreach(unit => {
    val positionProjected = if (lookaheadPixels == 0 || unit.is(Protoss.Dragoon)) unit.pixelCenter else unit.pixelCenter.radiateRadians(unit.angleRadians, lookaheadPixels)
    xMin = Math.min(xMin, positionProjected.x)
    yMin = Math.min(yMin, positionProjected.y)
    xMax = Math.max(xMax, positionProjected.x)
    yMax = Math.max(yMax, positionProjected.y)
  })
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