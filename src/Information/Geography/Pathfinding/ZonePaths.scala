package Information.Geography.Pathfinding

import Information.Geography.Types.Zone

import scala.collection.mutable

trait ZonePaths {
  
  private val paths = new mutable.HashMap[Zone, mutable.HashMap[Zone, Option[ZonePath]]]
  
  def zonePath(from: Zone, to: Zone): Option[ZonePath] = {
    
    if ( ! paths.contains(from)) {
      paths(from) = new mutable.HashMap[Zone, Option[ZonePath]]
    }
    
    if ( ! paths(from).contains(to)) {
      paths(from)(to) = ZonePathfinder.find(from, to)
    }
    
    paths(from)(to)
  }
}
