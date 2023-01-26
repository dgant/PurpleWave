package Micro.Heuristics

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
    lookaheadPixels: Double) {

  private val margin          = 8
  private val p               = target.pixel
  private val xs              = Seq(target.x - mx * margin, target.x + mx * (pixelWidth - margin))
  private val ys              = Seq(target.y - my * margin, target.y + my * (pixelHeight - margin))
  private val evalPixelStart  = Pixel(xs.min, ys.min)
  private val evalPixelEnd    = Pixel(xs.max, ys.max)

  val rectangle: PixelRectangle = PixelRectangle(evalPixelStart, evalPixelEnd)

  def units: Iterable[UnitInfo] = rectangle
    .pixelsEach32
    .map(_.tile)
    .filter(_.valid)
    .flatMap(_.units.view.filter(u =>
      SpellTargets.legal(u)
      && rectangle.contains(u.pixel))) // Require the unit to actually be there; don't trust the grid))

  val netValue: Double = units.view.map(_.spellTargetValue).sum
  var xMin: Int = Int.MaxValue
  var yMin: Int = Int.MaxValue
  var xMax: Int = Int.MinValue
  var yMax: Int = Int.MinValue
  if (units.isEmpty) {
    xMin = evalPixelStart.x
    yMin = evalPixelStart.y
    xMax = evalPixelEnd.x
    yMax = evalPixelEnd.y
  }
  units.foreach(unit => {
    val positionProjected = if (lookaheadPixels == 0 || Protoss.Dragoon(unit)) unit.pixel else unit.pixel.radiateRadians(unit.angleRadians, lookaheadPixels)
    xMin = Math.min(xMin, positionProjected.x)
    yMin = Math.min(yMin, positionProjected.y)
    xMax = Math.max(xMax, positionProjected.x)
    yMax = Math.max(yMax, positionProjected.y)
  })

  val finalTarget: Pixel = Pixel((xMin + xMax) / 2, (yMin + yMax) / 2)
}