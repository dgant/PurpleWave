package Micro.Heuristics

import Lifecycle.With
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
   projectionFrames    : Double = 0.0,
   candidates          : Option[Iterable[UnitInfo]] = None)
    : Option[Pixel] = {
    val finalCandidates = candidates.getOrElse(caster.matchups.allUnits
      .view
      .filter(target =>
            ((target.likelyStillThere && With.framesSince(target.lastSeen) < 72)
        ||  (target.burrowed && target.likelyStillThere)))
      .filter(_.pixelDistanceCenter(caster) <= searchRadiusPixels))
      .filter( ! _.invincible)
    val targets = finalCandidates.filter(evaluate(_, caster) > 0)
    val boxes = targets.flatMap(target => Seq(
      new AOETarget(target, caster,  1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, evaluate = evaluate),
      new AOETarget(target, caster, -1,  1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, evaluate = evaluate),
      new AOETarget(target, caster,  1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, evaluate = evaluate),
      new AOETarget(target, caster, -1, -1, pixelWidth = pixelWidth, pixelHeight = pixelHeight, lookaheadPixels = projectionFrames, evaluate = evaluate)))
    val bestBox = Maff.maxBy(boxes)(_.netValue).filter(_.netValue >= minimumValue)
    val output = bestBox.map(_.finalTarget)
    output
  }
}
