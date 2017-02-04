package Strategies.PositionFinders

import Caching.Cache
import Startup.With
import bwapi.{TilePosition, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.math._

class PositionProxy extends PositionFinder {
  
  var unitType = UnitType.Terran_Barracks
  var margin = 0
  
  val _cache = new Cache[Option[TilePosition]] { duration = 24 * 10; override def recalculate = _recalculate }
  override def find(): Option[TilePosition] = _cache.get
  
  def _recalculate(): Option[TilePosition] = {
    val ourStartLocation = BWTA.getStartLocation(With.game.self)
    val enemyStartLocations = BWTA.getStartLocations.asScala.filter(_ != ourStartLocation)
    
    val centroid:TilePosition =
      if(enemyStartLocations.size == 1) {
        
        //Fantastic. We know where they are so we can proxy as close to their base as we want.
        
        //In case the base has multiple chokepoints (like maps with backdoor expansion) get the one chokepoint closest to us
        val closestChoke = enemyStartLocations.head.getRegion.getChokepoints.asScala
          .toList
          .sortWith((a, b) =>
            a.getCenter.getPoint.getDistance(ourStartLocation.getPosition) <
            b.getCenter.getPoint.getDistance(ourStartLocation.getPosition))
          .map(_.getCenter.toTilePosition)
          .headOption
          .getOrElse(new TilePosition(0, 0))

        //We want to proxy a little bit away from the entrance to their base.
        //Here's a crude algorithm to do that by drawing a straight line from their choke to our base. It might fail on sufficiently twisty maps
        val weightThem = closestChoke.getDistance(ourStartLocation.getTilePosition)
        val weightUs = 12 //A bit further than the common sight distance of 7
        val x = round((closestChoke.getX * weightThem + ourStartLocation.getTilePosition.getX * weightUs) / (weightThem + weightUs)).toInt
        val y = round((closestChoke.getY * weightThem + ourStartLocation.getTilePosition.getY * weightUs) / (weightThem + weightUs)).toInt
        new TilePosition(x, y)
      }
      else {
        
        //Default: Position at the centroid of enemy bases
        new TilePosition(
          enemyStartLocations.map(_.getTilePosition.getX).sum / enemyStartLocations.size,
          enemyStartLocations.map(_.getTilePosition.getY).sum / enemyStartLocations.size)
      }
    
    With.architect.placeBuilding(unitType, centroid, margin, 30)
  }
}
