package Types.PositionFinders

import Processes.Architect
import Startup.With
import bwapi.{TilePosition, UnitType}
import bwta.BWTA

import scala.math._
import scala.collection.JavaConverters._

class PositionProxy extends PositionFinder {
  
  var unitType = UnitType.Terran_Barracks
  var margin = 0
  
  override def find(): Option[TilePosition] = {
    
    val ourStartLocation = BWTA.getBaseLocations.asScala
        .filter(_.isStartLocation)
        .filter(base => base.isStartLocation
          && With.ourUnits.exists(unit =>
            unit.getType.isResourceDepot
            && base.getRegion.getPolygon.isInside(unit.getPosition)))
        .head
    
    val enemyStartLocations = BWTA.getBaseLocations.asScala
      .filter(base => base.isStartLocation  && base != ourStartLocation)
  
    
    val centroid:TilePosition =
      if(enemyStartLocations.size == 1) {
        //Position outside chokepoint towards us
        val closestChoke = enemyStartLocations.head.getRegion.getChokepoints.asScala
          .toList
          .sortWith((a, b) =>
            a.getCenter.getPoint.getDistance(ourStartLocation.getPosition) <
            b.getCenter.getPoint.getDistance(ourStartLocation.getPosition))
          .map(_.getCenter.toTilePosition)
          .headOption
          .getOrElse(new TilePosition(0, 0))

        val weightThem = closestChoke.getDistance(ourStartLocation.getTilePosition)
        val weightUs = 12 //A bit further than the common sight distance of 7
        val x = round((closestChoke.getX * weightThem + ourStartLocation.getX * weightUs) / (weightThem + weightUs)).toInt
        val y = round((closestChoke.getY * weightThem + ourStartLocation.getY * weightUs) / (weightThem + weightUs)).toInt
        new TilePosition(x, y)
      } else {
        //Default: Position at the centroid of enemy bases
        new TilePosition(
          enemyStartLocations.map(_.getTilePosition.getX).sum / enemyStartLocations.size,
          enemyStartLocations.map(_.getTilePosition.getY).sum / enemyStartLocations.size)
      }
    
    Architect.placeBuilding(unitType, centroid, margin)
  }
}
