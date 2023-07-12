package Information.Geography.Calculations

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import Utilities.Time.Minutes

object UpdateZones {
  
  def apply(): Unit = {
    // Precalculate these
    With.geography.zones.foreach(_.distanceGrid)
    With.geography.zones.foreach(_.edges.foreach(_.distanceGrid))
    With.geography.zones.foreach(z => z.setUnits(With.units.all.filter(_.likelyStillThere).filter(_.zone == z).toVector))
    With.geography.zones.foreach(updateZone)
  
    if (With.frame == 0) {
      With.geography
        .startBases
        .foreach(startLocationBase =>
          Maff.minBy(With.geography.bases.view.filterNot(_.gas.isEmpty).filterNot(startLocationBase==))(_.zone.heart.groundTiles(startLocationBase.zone.heart))
            .foreach(natural => {
              natural.naturalOf = Some(startLocationBase)
              startLocationBase.natural = Some(natural)
            }))
    }
  
    With.geography.zones.foreach(_.setOwner(With.neutral))
    With.geography.zones.foreach(_.contested = false)
    val playerBorders = Players.all
      .filterNot(_.isNeutral)
      .map(player => (player, FindBorder(player)))
      .toMap
    playerBorders.foreach(pair => pair._2.foreach(zone =>
      if ( ! zone.owner.isNeutral || zone.contested) {
        zone.setOwner(With.neutral)
        zone.contested = true
      } else {
        zone.setOwner(pair._1)
      }))
  
    With.geography.home = Maff
      .minBy(With.geography.ourBases)(_.townHallTile.groundTiles(With.geography.home))
      .map(_.townHallTile)
      .getOrElse(With.geography.home)
  }

  def calculateExit(zone: Zone): Option[Edge] = {
    val enemyZone = zone.bases.exists(_.isEnemy) && ! zone.bases.exists(_.isOurs)
    // Take the edge closest to opposing production
    Maff.minBy(zone.edges)(edge =>
        Maff.orElse(
          (if (enemyZone) Seq(With.geography.ourMain.heart)   else With.scouting.enemyMain.map(_.heart).toSeq),
          (if (enemyZone) Seq(With.scouting.ourThreatOrigin)  else Seq(With.scouting.enemyThreatOrigin)))
        .map(edge.distanceGrid.get))
  }
  def calculateEntrance(zone: Zone): Option[Edge] = {
    Maff.minBy(zone.edges)(edge => With.geography.startLocations.map(edge.distanceGrid.get).min)
  }

  private val wallBuildingThresholdDistanceSquared = Math.pow(32 * 12, 2)
  private def updateZone(zone: Zone): Unit = {
    zone.distanceGrid.initialize()
    zone.edges.foreach(_.distanceGrid.initialize())
    zone.bases.foreach(UpdateBase(_))
    zone.exitNow = calculateExit(zone)
    zone.entranceNow = calculateEntrance(zone)
  
    val exitBuildings = zone.exitOriginal.map(exit =>
      zone.units
        .filter(_.pixelDistanceSquared(exit.pixelCenter) < wallBuildingThresholdDistanceSquared)
        .filter(u => u.unitClass.isBuilding && ! u.flying))
      .getOrElse(List.empty)
    lazy val canaryTileInside   = zone.tiles.find(With.grids.walkable.get)
    lazy val canaryTileOutside  = zone.exitOriginal.map(_.otherSideof(zone)).flatMap(_.tiles.find(With.grids.walkable.get))
    zone.walledIn = (
      With.frame < Minutes(6)()
      && exitBuildings.exists(_.isAny(Terran.SupplyDepot, Terran.EngineeringBay))
      && exitBuildings.exists(Terran.Barracks)
      && canaryTileInside.exists(tileInside =>
          canaryTileOutside.exists(tileOutside =>
            ! new PathfindProfile(tileInside, end = Some(tileOutside), lengthMaximum = Some(100)).find.pathExists)))
  }
}
