package Information.Geography.Calculations

import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.Players.PlayerInfo

import scala.collection.mutable

object BorderFinder {
  
  def claimedZones(player: PlayerInfo): Set[Zone] = {
    
    // The goal: Find the set of Zones that a player's bases enclose
    
    val output = new mutable.HashSet[Zone]
    
    // Start with our bases
    val playerZones = With.geography.bases.filter(base => base.owner == player).map(_.zone)
    val occupiedZones = With.units.all
      .filter(unit =>
        unit.unitClass.isBuilding
        && unit.player == player
        && ! unit.flying)
      .map(_.pixelCenter.zone)
      .filter(_.bases.nonEmpty)
    output ++= playerZones
    output ++= occupiedZones // Specifically, we want to include bases that are getting static defense before being taken
    
    // Include all zones along the shortest paths between the bases
    val travelZones =
      playerZones.flatten(zone1 =>
        playerZones.flatten(zone2 =>
          if (zone1 == zone2)
            None
          else
            With.paths.zonePath(zone1, zone2)
        ))
        .flatMap(_.steps.map(_.from))
    output ++= travelZones
    
    // Lastly, include all zones which can only be reached from the enclosed zones
    var encompassedZones: Iterable[Zone] = None
    do {
      encompassedZones = With.geography.zones.toSet.diff(output).filter(_.edges.forall(_.zones.exists(output.contains)))
      output ++= encompassedZones
    } while(encompassedZones.nonEmpty)
    
    output.toSet
  }
  
}
