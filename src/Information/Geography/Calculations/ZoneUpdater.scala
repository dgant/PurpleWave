package Information.Geography.Calculations

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.Zone
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import Utilities.ByOption

object ZoneUpdater {
  
  def update() {
    if (With.frame == 0) {
      // Grid tasks update for the first time after Geography because many need to understand zones
      // But for us to initialize zones properly, we need the unit grid ready.
      With.grids.units.initialize()
      With.grids.units.update()
      // Precalculate these
      With.geography.zones.foreach(_.distanceGrid)
      With.geography.zones.foreach(_.edges.foreach(_.distanceGrid))
    }

    With.geography.zones.foreach(_.unitBuffer.clear())
    for (unit <- With.units.all) {
      unit.zone.unitBuffer += unit
    }
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
  
    val plannedBases = With.groundskeeper.proposalPlacements
      .flatMap(placement => placement._2.tile)
      .flatMap(tile => if (tile.zone.bases.isEmpty) None else Some(tile.zone.bases.minBy(_.heart.tileDistanceFast(tile))))
      .filter(_.owner.isNeutral)
      .toSet
  
    With.geography.zones.foreach(zone => { zone.owner = With.neutral; zone.contested = false })
    val playerBorders = Players.all
      .filterNot(_.isNeutral)
      .map(player => (player, BorderFinder.claimedZones(player)))
      .toMap
    
    playerBorders.foreach(pair => pair._2.foreach(zone => {
      if ( ! zone.owner.isNeutral || zone.contested) {
        zone.owner = With.neutral
        zone.contested = true
      }
      else {
        zone.owner = pair._1
      }
    }))
  
    With.geography.home = ByOption.minBy(With.geography.ourBases)(_.isStartLocation)
      .map(_.townHallArea.startInclusive)
      .getOrElse(SpecificPoints.tileMiddle)
  }

  private val wallBuildingThresholdDistanceSquared = Math.pow(32 * 12, 2)
  def updateZone(zone: Zone) {
    zone.units = zone.unitBuffer.toVector
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
      With.frame < GameTime(10, 0)()
      && exitBuildings.exists(_.isAny(Terran.SupplyDepot, Terran.EngineeringBay))
      && exitBuildings.exists(_.is(Terran.Barracks))
      && canaryTileInside.exists(tileInside =>
          canaryTileOutside.exists(tileOutside =>
            ! new PathfindProfile(
                tileInside,
                end = Some(tileOutside),
                lengthMaximum = Some(100))
              .find.pathExists)))
  }
  
  
}
