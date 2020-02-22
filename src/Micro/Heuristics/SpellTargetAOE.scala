package Micro.Heuristics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRectangle}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object SpellTargetAOE {
  
  def chooseTargetPixel(
   caster              : FriendlyUnitInfo,
   searchRadiusPixels  : Double,
   minimumValue        : Double,
   evaluate            : (UnitInfo, FriendlyUnitInfo) => Double,
   pixelWidth          : Int,
   pixelHeight         : Int,
   projectionFrames    : Double = 0.0,
   candidates          : Option[Iterable[UnitInfo]] = None)
    : Option[Pixel] = {

    case class TargetBox(unit: UnitInfo, mx: Int, my: Int) {
      private val p = unit.pixelCenter
      private val xs = Seq(unit.x, unit.x + mx * pixelWidth)
      private val ys = Seq(unit.y, unit.y + my * pixelHeight)
      private val evalPixelStart = Pixel(xs.min, ys.min)
      private val evalPixelEnd = Pixel(xs.max, ys.max)
      val rectangle = PixelRectangle(evalPixelStart, evalPixelEnd)
      val units: Set[UnitInfo] = rectangle.pixelsEach32.view.map(_.tile).filter(_.valid).flatMap(With.grids.units.get(_).filter(u => u.likelyStillThere && rectangle.contains(u.pixelCenter))).toSet
      val netValue: Double = units.view.map(evaluate(_, caster)).sum
      lazy val xMin: Int = ByOption.min(units.view.map(_.x)).getOrElse(p.x)
      lazy val yMin: Int = ByOption.min(units.view.map(_.x)).getOrElse(p.y)
      lazy val xMax: Int = ByOption.max(units.view.map(_.x)).getOrElse(p.x)
      lazy val yMax: Int = ByOption.max(units.view.map(_.x)).getOrElse(p.y)
      lazy val finalTarget = Pixel((xMin + xMax) / 2, (yMin + yMax) / 2)

      def drawMap(): Unit = {
        val color = Colors.NeonTeal
        DrawMap.box(evalPixelStart, evalPixelEnd, color)
        DrawMap.drawStar(finalTarget, 12, color)
        units.foreach(unit => {
          DrawMap.circle(unit.pixelCenter, unit.unitClass.dimensionMin, color)
          DrawMap.label(evaluate(unit, caster).toString, unit.pixelCenter.subtract(0, unit.unitClass.dimensionUp + 16))
        })
      }
    }

    val finalCandidates = candidates.getOrElse(caster.matchups.allUnits
      .view
      .filter(target => (target.visible || target.burrowed))
      .filter(_.pixelDistanceCenter(caster) <= searchRadiusPixels))
      .filter( ! _.invincible)
    val targets = finalCandidates.filter(evaluate(_, caster) > 0)
    val boxes = targets.flatMap(target => Seq(
      TargetBox(target,  1,  1),
      TargetBox(target, -1,  1),
      TargetBox(target,  1, -1),
      TargetBox(target, -1, -1)))
    val bestBox = ByOption.maxBy(boxes)(_.netValue)
    val output = bestBox.map(_.finalTarget)
    bestBox.foreach(box => With.animations.addMap(box.drawMap))
    output
  }
}
