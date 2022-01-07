package Information.Geography.Calculations

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.SpecificPoints
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import Utilities.Time.Minutes

object ZoneUpdater {
  
  def update() {
    // Precalculate these
    With.geography.zones.foreach(_.distanceGrid)
    With.geography.zones.foreach(_.edges.foreach(_.distanceGrid))
    With.geography.zones.foreach(_.unitBuffer.clear())
    With.units.all.filter(_.likelyStillThere).foreach(unit => unit.zone.unitBuffer += unit)
    With.geography.zones.foreach(updateZone)
  
    if ( ! With.geography.naturalsSearched) {
      With.geography.naturalsSearched = true
      With.geography.bases
        .filter(_.isStartLocation)
        .foreach(startLocationBase =>
          With.geography.bases
            .filter(otherBase => otherBase != startLocationBase && otherBase.gas.nonEmpty)
            .sortBy(_.zone.distancePixels(startLocationBase.zone))
            .headOption
            .foreach(_.isNaturalOf = Some(startLocationBase)))
    }
  
    With.geography.zones.foreach(zone => { zone.owner = With.neutral; zone.contested = false })
    val playerBorders = Players.all
      .filterNot(_.isNeutral)
      .map(player => (player, BorderFinder.claimedZones(player)))
      .toMap
    playerBorders.foreach(pair => pair._2.foreach(zone => {
      if ( ! zone.owner.isNeutral || zone.contested) {
        zone.owner = With.neutral
        zone.contested = true
      } else {
        zone.owner = pair._1
      }
    }))
  
    With.geography.home = Maff
      .minBy(With.geography.ourBases)(_.townHallTile.walkableTile.groundTiles(With.geography.home.walkableTile))
      .map(_.townHallTile)
      .getOrElse(SpecificPoints.tileMiddle)
  }

  private val wallBuildingThresholdDistanceSquared = Math.pow(32 * 12, 2)
  private def updateZone(zone: Zone) {
    zone.distanceGrid.initialize()
    zone.edges.foreach(_.distanceGrid.initialize())
    zone.exitDistanceGrid.initialize()
    zone.bases.foreach(BaseUpdater.updateBase)
    zone.exitNow = zone.calculateExit
  
    val exitBuildings = zone.exit.map(exit =>
      zone.units
        .filter(_.pixelDistanceSquared(exit.pixelCenter) < wallBuildingThresholdDistanceSquared)
        .filter(u => u.unitClass.isBuilding && ! u.flying))
      .getOrElse(List.empty)
    lazy val canaryTileInside   = zone.tiles.find(With.grids.walkable.get)
    lazy val canaryTileOutside  = zone.exit.map(_.otherSideof(zone)).flatMap(_.tiles.find(With.grids.walkable.get))
    zone.walledIn = (
      With.frame < Minutes(6)()
      && exitBuildings.exists(_.isAny(Terran.SupplyDepot, Terran.EngineeringBay))
      && exitBuildings.exists(Terran.Barracks)
      && canaryTileInside.exists(tileInside =>
          canaryTileOutside.exists(tileOutside =>
            ! new PathfindProfile(tileInside, end = Some(tileOutside), lengthMaximum = Some(100)).find.pathExists)))
  }
  
  
}
