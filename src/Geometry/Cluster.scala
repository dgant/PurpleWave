package Geometry

import Types.UnitInfo.UnitInfo
import Utilities.Enrichment.EnrichPosition._
import scala.collection.mutable

object Cluster {
  
  def generate(
    units:Iterable[UnitInfo],
    radius:Int)
      :mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] = {
    
    val neighborsByUnit = _mapUnitsToNeighbors(units, radius)
    val unitLeaders = new mutable.HashMap[UnitInfo, UnitInfo]
    val groupsByLeader = new mutable.HashMap[UnitInfo, mutable.HashSet[UnitInfo]] {
      override def default(key: UnitInfo):mutable.HashSet[UnitInfo] = {
        put(key, new mutable.HashSet[UnitInfo])
        this(key)}}
    
    units.foreach(leader => {
      if ( ! unitLeaders.contains(leader)) {
        groupsByLeader(leader).add(leader)
        groupsByLeader(leader) ++= neighborsByUnit(leader)
        groupsByLeader(leader).foreach(groupMember => unitLeaders.put(groupMember, leader))
      }})
    
    return groupsByLeader
  }
  
  def _mapUnitsToNeighbors(
    units:Iterable[UnitInfo],
    radius:Int)
      :Map[UnitInfo, Iterable[UnitInfo]] = {
    
    val radiusSquared = radius * radius
    
    //Yes, this includes the unit itself
    units.map(unit => (unit, units.filter(_.position.distanceSquared(unit.position) <= radiusSquared))).toMap
  }
}
