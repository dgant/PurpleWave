package Micro.Heuristics.Spells

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

object TargetAOE {
  
  def chooseTarget(
    caster              : UnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo) => Double): Option[Pixel] = {
    
    val targets       = caster.matchups.allUnits.filter(target => (target.visible || target.burrowed) && target.pixelDistanceFast(caster) <= searchRadiusPixels)
    val targetsByTile = targets.groupBy(_.tileIncludingCenter)
    val targetValues  = targets.map(target => (target, evaluate(target))).toMap
    
    val valueByTile = targetsByTile.keys
      .map(tile => (
        tile,
        tile
          .adjacent9
          .flatMap(targetsByTile.get)
          .flatten
          .map(targetValues).sum))
      .toMap
    
    if (valueByTile.nonEmpty) {
      val bestTile = valueByTile.maxBy(_._2)
      if (bestTile._2 > minimumValue) {
        val tile = bestTile._1
        val finalTargets = tile.adjacent9.flatMap(targetsByTile.get).flatten.toSet
        val finalPixels   = finalTargets.map(_.pixelCenter)
        if (finalPixels.nonEmpty) { // Safety valve check
          val centroid = finalPixels.centroid
          return Some(centroid)
        }
      }
    }
    
    None
  }
}
