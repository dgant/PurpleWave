package Information.Geography.Calculations

import Information.Geography.Types.Zone
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran

object ZoneUpdater {
  
  def update() {
    // Grid tasks update for the first time after Geography because many need to understand zones
    // But for us to initialize zones properly, we need the unit grid ready.
    if (With.frame == 0) {
      With.grids.units.initialize()
      With.grids.units.update()
    }

    With.geography.zones.foreach(_.unitBuffer.clear())
    for (unit <- With.units.all) {
      unit.zone.unitBuffer += unit
    }
    With.geography.zones.foreach(z => z.units = z.unitBuffer.toVector)
    With.geography.zones.foreach(updateZone)
  
    if ( ! With.geography.naturalsSearched) {
      With.geography.naturalsSearched = true
      With.geography.bases
        .filter(_.isStartLocation)
        .foreach(startLocationBase =>
          With.geography.bases
            .filter(otherBase => otherBase != startLocationBase && otherBase.gas.nonEmpty)
            .toVector
            .sortBy(_.zone.distancePixels(startLocationBase.zone))
            .headOption
            .foreach(_.isNaturalOf = Some(startLocationBase)))
    }
  
    val plannedBases = With.groundskeeper.proposalPlacements
      .flatMap(placement => placement._2.tile)
      .flatMap(tile => if (tile.zone.bases.isEmpty) None else Some(tile.zone.bases.minBy(_.heart.tileDistanceFast(tile))))
      .filter(_.owner.isNeutral)
      .toSet
  
    With.geography.zones.foreach(zone =>  { zone.owner = With.neutral; zone.contested = false })
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
  
    With.geography.home = With.geography.ourBases
      .toVector
      .sortBy( ! _.isStartLocation)
      .headOption
      .map(_.townHallArea.startInclusive)
      .getOrElse(SpecificPoints.tileMiddle)
  }
  
  def updateZone(zone: Zone) {
    zone.distanceGrid.initialize()
    zone.bases.foreach(BaseUpdater.updateBase)
  
    val exitBuildings = zone.exit.map(exit =>
      zone.units
        .filter(_.pixelDistanceSquared(exit.pixelCenter) < 100)
        .filter(u => u.unitClass.isBuilding && ! u.flying))
      .getOrElse(List.empty)
  
    lazy val canaryTileInside   = zone.tiles.find(With.grids.walkable.get)
    lazy val canaryTileOutside  = zone.exit.map(_.otherSideof(zone)).flatMap(_.tiles.find(With.grids.walkable.get))
    zone.walledIn = (
      With.frame < GameTime(10, 0)()
      && exitBuildings.exists(_.is(Terran.SupplyDepot))
      && exitBuildings.exists(_.is(Terran.Barracks))
      && canaryTileInside.exists(tileInside =>
          canaryTileOutside.exists(tileOutside =>
            ! With.paths.manhattanGroundDistanceThroughObstacles(
              tileInside,
              tileOutside,
              obstacles = Set.empty,
              maximumDistance = 100).pathExists)))
  }
  
  
}
