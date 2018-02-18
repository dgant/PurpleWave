package Information.Geography.Pathfinding

import Information.Geography.Types.Zone

import scala.collection.mutable

trait ZonePaths {
  
  private val paths = new mutable.HashMap[Zone, mutable.HashMap[Zone, Option[ZonePath]]]
  
  def zoneDistance(from: Zone, to: Zone): Double = {
    zonePath(from, to).map(_.lengthPixels).getOrElse(Double.PositiveInfinity)
  }
  
  def zonePath(from: Zone, to: Zone): Option[ZonePath] = {
    
    if ( ! paths.contains(from)) {
      paths(from) = new mutable.HashMap[Zone, Option[ZonePath]]
    }
    
    if ( ! paths(from).contains(to)) {
      paths(from)(to) = ZonePathfinder.find(from, from, to)
    }
    
    paths(from)(to)
  }
}
