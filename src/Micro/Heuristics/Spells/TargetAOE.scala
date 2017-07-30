package Micro.Heuristics.Spells

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

object TargetAOE {
  
  def chooseTarget(
    caster              : UnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo) => Double): Option[Pixel] = {
    
    val targets       = caster.matchups.allUnits.filter(_.pixelDistanceFast(caster) < searchRadiusPixels)
    val targetsByTile = targets.groupBy(_.project(6).tileIncluding)
    val targetValues  = targets.map(target => (target, evaluate(target))).toMap
    
    val valueByTile = targetsByTile.keys
      .map(tile => (
        tile,
        tile
          .adjacent8
          .flatMap(targetsByTile.get)
          .flatten
          .map(targetValues).sum))
      .toMap
    
    if (valueByTile.nonEmpty) {
      val bestTile = valueByTile.maxBy(_._2)
      if (bestTile._2 > minimumValue) {
        return Some(bestTile._1.pixelCenter)
      }
    }
    
    None
  }
}
