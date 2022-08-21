package Micro.Heuristics

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
    candidates.foreach(t => t.spellTargetValue = evaluate(t, caster))
    val targets = candidates.filter(_.spellTargetValue > 0)
    val boxes = targets.flatMap(target => Seq(
      new AOETarget(target, caster,  1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames),
      new AOETarget(target, caster, -1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames),
      new AOETarget(target, caster,  1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames),
      new AOETarget(target, caster, -1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames)))
    val bestBox = Maff.maxBy(boxes)(_.netValue).filter(_.netValue >= minimumValue)
    val output = bestBox.map(_.finalTarget)
    output
  }
}
