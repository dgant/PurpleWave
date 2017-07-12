package Information.Geography.Calculations

import Information.Geography.Pathfinding.GroundPathFinder
import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

object ZoneUpdater {
  
  def update() {
    With.geography.zones.foreach(updateZone)
  
    // TODO: We only want to do this once!
    With.geography.bases
      .filter(_.isStartLocation)
      .foreach(startLocationBase =>
        With.geography.bases
          .filter(otherBase => otherBase != startLocationBase && otherBase.gas.nonEmpty)
          .toVector
          .sortBy(
            _.townHallArea.startInclusive.groundPixelsByTile(
              startLocationBase.townHallArea.startInclusive))
          .headOption
          .foreach(_.isNaturalOf = Some(startLocationBase)))
  
    val plannedBases = With.groundskeeper.proposalPlacements
      .flatMap(placement => placement._2.tile)
      .flatMap(tile => if (tile.zone.bases.isEmpty) None else Some(tile.zone.bases.minBy(_.heart.tileDistanceFast(tile))))
      .filter(_.owner.isNeutral)
      .toSet
    
    With.geography.bases.foreach(base => base.planningToTake = plannedBases.contains(base))
  
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
  }
  
  def updateZone(zone: Zone) {
    zone.bases.foreach(updateBase)
  }
  
  private def updateBase(base: Base) {
    updateTownHall(base)
    updateOwner(base)
    updateAssets(base)
  }
  
  private def updateTownHall(base: Base) {
    base.townHall = None
    val townHalls = With.units.buildings
      .filter(unit =>
        unit.unitClass.isTownHall
        && unit.tileIncludingCenter.zone == base.zone
        && base.zone.bases.minBy(_.heart.tileDistanceFast(unit.tileIncludingCenter)) == base)
    
    if (townHalls.nonEmpty) {
      base.townHall = Some(townHalls.minBy(_.pixelDistanceSquared(base.townHallArea.midPixel)))
    }
  }
  
  private def updateOwner(base: Base) {
    
    base.owner = base.townHall.map(_.player).getOrElse(With.neutral)
    
    // Assume ownership of occupied base we haven't seen lately
    if (base.owner.isNeutral && base.lastScoutedFrame < With.framesSince(Protoss.Nexus.buildFrames)) {
      With.units.enemy
        .find(unit => ! unit.flying && unit.unitClass.isBuilding && unit.pixelCenter.zone == base.zone)
        .foreach(enemyBuilding => base.owner = enemyBuilding.player)
    }
    
    // Assume ownership of unscouted main from natural
    if (base.owner.isNeutral && base.lastScoutedFrame <= 0) {
      val natural = With.geography.bases.find(_.isNaturalOf.contains(base))
      natural.foreach(someNatural => base.owner = someNatural.owner)
    }
  }
  
  private def updateAssets(base: Base) {
    
    base.minerals       = With.units.neutral.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold).filter(resourceIsInBase(_, base)).toSet
    base.gas            = With.units.all.filter(_.unitClass.isGas).filter(resourceIsInBase(_, base)).toSet
    base.workers        = With.units.all.filter(unit => unit.unitClass.isWorker && base.zone.contains(unit.pixelCenter))
    base.mineralsLeft   = base.minerals.filter(_.alive).toVector.map(_.mineralsLeft).sum
    base.gasLeft        = base.gas.filter(_.alive).toVector.map(_.gasLeft).sum
    base.harvestingArea = (Vector(base.townHallArea) ++ (base.minerals.filter(_.mineralsLeft > With.configuration.blockerMineralThreshold) ++ base.gas).map(_.tileArea)).boundary
    base.heart          = base.harvestingArea.midpoint
    
    val exitBuildings = base.zone.exit.map(exit =>
      With.units
        .inTileRadius(exit.centerPixel.tileIncluding, 6)
        .filter(u => u.unitClass.isBuilding && ! u.flying))
      .getOrElse(List.empty)
    
    lazy val canaryTileInside   = base.zone.tiles.find(With.grids.walkable.get)
    lazy val canaryTileOutside  = base.zone.exit.map(_.otherSideof(base.zone)).flatMap(_.tiles.find(With.grids.walkable.get))
    base.walledIn =
      exitBuildings.count(_.is(Terran.SupplyDepot))  >= 1 &&
      exitBuildings.count(_.is(Terran.Barracks))     >= 1 &&
      canaryTileInside.exists(tileInside =>
        canaryTileOutside.exists(tileOutside =>
           ! GroundPathFinder.manhattanGroundDistanceThroughObstacles(
             tileInside,
             tileOutside,
             obstacles = Set.empty,
             maximumDistance = 100).pathExists))
  }
  
  private def resourceIsInBase(resource: UnitInfo, base: Base): Boolean = {
    resource.pixelCenter.zone == base.townHallTile.zone &&
    resource.pixelDistanceFast(base.townHallArea.midPixel) < With.configuration.baseRadiusPixels
  }
}
