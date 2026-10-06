package Micro.Heuristics

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class SpellTargetAOE {

  def chooseTargetPixel(
    caster              : FriendlyUnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo, FriendlyUnitInfo) => Double,
    pixelWidth          : Int,
    pixelHeight         : Int,
    projectionFrames    : Double = 0.0)
      : Option[Pixel] = {

    val candidates = SpellTargets(caster).filter(_.pixelDistanceCenter(caster) <= searchRadiusPixels)
    val values = new mutable.HashMap[UnitInfo, Double]
    def valueUnit(unit: UnitInfo): Double = values.getOrElseUpdate(unit, evaluate(unit, caster))
    val targets = candidates.filter(valueUnit(_) > 0)
    val boxes = targets.flatMap(target => Seq(
      new AOETarget(target, caster,  1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, valueUnit = valueUnit),
      new AOETarget(target, caster, -1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, valueUnit = valueUnit),
      new AOETarget(target, caster,  1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, valueUnit = valueUnit),
      new AOETarget(target, caster, -1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, valueUnit = valueUnit)))
    val bestBox = Maff.maxBy(boxes)(_.netValue).filter(_.netValue >= minimumValue)
    val output = bestBox.map(_.finalTarget)
    output
  }
}
