package Geometry

import Startup.With
import Types.UnitInfo.UnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.Position

import scala.collection.mutable

object Clustering {
  
  def groupUnits(
    units:Iterable[UnitInfo],
    radius:Int,
    limitRegion:Boolean = false)
      :mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] = {
    group(units, radius, limitRegion, (u) => u.position)
  }
  
  def group[T](
    things:Iterable[T],
    radius:Int,
    limitRegion:Boolean = false,
    extractPosition:(T) => Position)
      :mutable.HashMap[T, mutable.HashSet[T]] = {
    
    val neighborsByUnit = _mapUnitsToNeighbors(things, radius, extractPosition)
    val unitLeaders = new mutable.HashMap[T, T]
    val groupsByLeader = new mutable.HashMap[T, mutable.HashSet[T]] {
      override def default(key: T):mutable.HashSet[T] = {
        put(key, new mutable.HashSet[T])
        this(key)}}
    
    things.foreach(thing => {
      if ( ! unitLeaders.contains(thing)) {
        groupsByLeader(thing).add(thing)
        groupsByLeader(thing) ++= neighborsByUnit(thing).filter(neighbor =>
          ! limitRegion ||
            With.game.getGroundHeight(extractPosition(thing).toTilePosition) ==
            With.game.getGroundHeight(extractPosition(neighbor).toTilePosition))
        groupsByLeader(thing).foreach(groupMember => unitLeaders.put(groupMember, thing))
      }})
    
    return groupsByLeader
  }
  
  def _mapUnitsToNeighbors[T](
    things:Iterable[T],
    radius:Int,
    extractPosition:(T) => Position)
      :Map[T, Iterable[T]] = {
    
    val radiusSquared = radius * radius
    
    //Yes, this includes the unit itself
    things.map(thing => (thing, things.filter(extractPosition(_).pixelDistanceSquared(extractPosition(thing)) <= radiusSquared))).toMap
  }
}
