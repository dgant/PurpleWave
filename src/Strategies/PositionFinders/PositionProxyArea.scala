package Strategies.PositionFinders

import Caching.PermanentCache
import Startup.With
import bwapi.{TilePosition, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.math._

class PositionProxyArea extends PositionFinder {
  
  var unitType = UnitType.Protoss_Pylon
  var margin = 4
  
  val _cache = new PermanentCache[Option[TilePosition]] { override def recalculate = _recalculate }
  override def find(): Option[TilePosition] = _cache.get
  
  def _recalculate(): Option[TilePosition] = {
    val ourStartLocation = BWTA.getStartLocation(With.game.self)
    val enemyStartLocations = With.scout.unexploredStartLocations()
    
    val centroid:TilePosition =
      if(enemyStartLocations.size == 0) {
        //WTF, but let's work with it; otherwise we're just going to divide by zero and choke when we calculate the centroid later
        new TilePosition(
          With.game.mapWidth / 2,
          With.game.mapHeight / 2)
      }
      else if(enemyStartLocations.size == 1) {
        
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
        val weightUs = 24 //This should get us out of their base
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
