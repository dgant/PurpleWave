package Micro.Heuristics.Spells

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

object TargetAOE {
  
  def projectTarget(target: UnitInfo, framesAhead: Double): Pixel = {
    target.projectFrames(framesAhead)
  }
  
  def chooseTargetPixel(
    caster              : UnitInfo,
    searchRadiusPixels  : Double,
    minimumValue        : Double,
    evaluate            : (UnitInfo) => Double,
    projectionFrames    : Double = 0.0,
    tileMapper          : (Tile) => Iterable[Tile] = _.adjacent9,
    candidates          : Option[Iterable[UnitInfo]] = None)
    : Option[Pixel] = {
    
    val targets = candidates
      .getOrElse(caster.matchups.allUnits.filter(target =>
        (target.visible || target.burrowed)
        && target.pixelDistanceCenter(caster) <= searchRadiusPixels))
      .filter( ! _.invincible)
    val targetsByTile = targets.groupBy(projectTarget(_, projectionFrames).tileIncluding)
    val targetValues  = targets.map(target => (target, evaluate(target))).toMap
    
    val valueByTile = targetsByTile.keys
      .map(tile => (
        tile,
        tileMapper(tile)
          .flatMap(targetsByTile.get)
          .flatten
          .map(targetValues).sum))
      .toMap
    
    if (valueByTile.nonEmpty) {
      val bestTile = valueByTile.maxBy(_._2)
      if (bestTile._2 >= minimumValue) {
        val tile          = bestTile._1
        val tiles         = tileMapper(tile)
        val finalTargets  = tiles.flatMap(targetsByTile.get).flatten.toSet
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
