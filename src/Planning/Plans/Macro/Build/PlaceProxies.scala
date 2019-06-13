package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

abstract class PlaceProxies(buildings: UnitClass*) extends Plan {

  val tileScore: Tile => Double

  val proxyTargets = new Cache(() =>
    if (With.geography.enemyBases.nonEmpty) {
      Seq(With.geography.enemyBases.minBy(b => (if (b.isStartLocation) 1 else 10)).townHallTile)
    } else {
      With.geography.startBases.filterNot(_.owner.isUs).map(_.townHallTile)
    }
  )

  def groundDistanceMax(tile: Tile): Double = {
    proxyTargets().map(_.zone.distanceGrid.get(tile).toDouble).max
  }

  def airDistanceMax(tile: Tile): Double = {
    proxyTargets().map(_.tileDistanceFast(tile)).max
  }

  val minSight: Double = Terran.SCV.sightRangePixels / 32
  val minDistance: Double = 40
  val groundProxyScore: Tile => Double = tile => {
    (
      (if (With.grids.enemyVision.isSet(tile)) 1 else 10)
      * With.grids.altitudeBonus.get(tile)
      * Math.max(minSight, With.grids.scoutingPathsBases.get(tile))
      * Math.max(minSight, With.grids.scoutingPathsStartLocations.get(tile))
      / Math.max(minDistance, groundDistanceMax(tile))
      / Math.max(minDistance, groundDistanceMax(tile))
      / Math.max(minDistance, groundDistanceMax(tile))
    )
  }

  val airProxyScore: Tile => Double = tile => {
    val airDistanceMax = this.airDistanceMax(tile)
    (
      (if (With.grids.enemyVision.isSet(tile)) 1 else 10)
      * With.grids.altitudeBonus.get(tile)
      * Math.max(minSight, With.grids.scoutingPathsBases.get(tile))
      * Math.max(minSight, With.grids.scoutingPathsStartLocations.get(tile))
      * Math.max(minDistance, groundDistanceMax(tile))
      / Math.max(minDistance, airDistanceMax)
      / Math.max(minDistance, airDistanceMax)
      / Math.max(minDistance, airDistanceMax)
    )
  }

  val techProxyScore: Tile => Double = tile => {
    val target = With.intelligence.threatOrigin
    (
      (if (With.grids.enemyVision.isSet(tile)) 1 else 10)
      * With.grids.altitudeBonus.get(tile)
      * Math.max(minSight, With.grids.scoutingPathsBases.get(tile))
      * Math.max(minSight, With.grids.scoutingPathsStartLocations.get(tile))
      * Math.max(minDistance, groundDistanceMax(tile))
    )
  }

  var placements: Option[Seq[Blueprint]] = None
  override def onUpdate(): Unit = {
    // HACK: Why are these not initialized?
    With.grids.scoutingPathsBases.update()
    With.grids.scoutingPathsStartLocations.update()
    placements = placements.orElse(Some(calculatePlacements))
    placements.foreach(_.foreach(With.groundskeeper.suggest))
  }

  def calculatePlacements: Seq[Blueprint] = {
    // Our placement logic can't try-and-place multiple buildings that rely on each other.
    // So the workaround is to try to fit all the buildings we need into one block, then
    // manually try to place that block.
    //
    // Approximately what that block should look like:
    // P = Pylon
    // # = Building requiring Pylon
    // + = Walkable margin, if required
    //
    // ++++++++++++
    // +1111  3333+
    // +1111PP3333+
    // +1111PP3333+
    // +2222  4444+
    // +2222  4444+
    // +2222  4444+
    // ++++++++++++

    val walkableMargin  = if (buildings.exists(_.trainsGroundUnits)) 1 else 0
    val countPylons     = Math.max(buildings.count(_ == Protoss.Pylon), (buildings.count(_.requiresPsi) + 3) / 4)
    val countNonPylons  = buildings.count(_ != Protoss.Pylon)
    val buildableWidth  = 2 * countPylons + 4 * (1 + countNonPylons) / 2
    val buildableHeight = 3 * Math.min(2, countNonPylons)
    val walkableWidth   = buildableWidth + 2 * walkableMargin
    val walkableHeight  = buildableHeight + 2 * walkableMargin

    val grid = With.grids.disposableInt()
    val mapTileWidth = With.mapTileWidth
    val xLimit = With.mapTileWidth - walkableWidth
    val yLimit = With.mapTileHeight - walkableHeight
    var bestX = -1
    var bestY = -1
    var bestScore = Double.MinValue
    var x = 0
    while (x < xLimit) {
      var y = 0
      while (y < yLimit) {

        // Check if this is a valid area
        var valid = true
        var areaX = 0
        while (areaX < walkableWidth) {
          var areaY = 0
          while (areaY < walkableHeight) {
            val i = x + areaX + mapTileWidth * (y + areaY)
            valid = (
              (! new Tile(i).zone.island) && (
              if (areaX < walkableMargin
                || areaY < walkableMargin
                || areaX > walkableWidth - walkableMargin
                || areaY > walkableHeight - walkableMargin) {
                With.grids.walkable.get(i)
              } else {
                With.grids.buildable.get(i)
              }))
            if (!valid) {
              // Break out of the loop
              areaX = walkableWidth
              areaY = walkableHeight
            }
            areaY += 1
          }
          areaX += 1
        }

        // Score valid placements
        if (valid) {
          var score = Double.MaxValue
          var areaX = 0
          while (areaX < walkableWidth) {
            var areaY = 0
            while (areaY < walkableHeight) {
              score = Math.min(score, tileScore(Tile(x+areaX, y+areaY)))
              areaY += 1
            }
            areaX += 1
          }
          if (score > bestScore) {
            bestScore = score
            bestX = x
            bestY = y
          }
        }

        y += 1
      }
      x += 1
    }

    // We failed to find a valid area
    if (bestScore == Double.MinValue) {
      return Seq.empty
    }

    // Generate blueprints from the best available placement
    val blueprints = new ArrayBuffer[Blueprint]
    (0 until countPylons).foreach(i => {
      blueprints += new Blueprint(
        building = Some(Protoss.Pylon),
        requirePower = Some(false),
        requireCreep = Some(false),
        requireTownHallTile = Some(false),
        marginPixels = Some(0),
        respectHarvesting = Some(Tile(bestX, bestY).base.exists(b => b.owner.isUs || b.isNaturalOf.exists(_.owner.isUs))),
        requireCandidates = Some(Seq(Tile(
          bestX + walkableMargin + 4 + 6 * i,
          bestY + walkableHeight + 1
        ))),
        forcePlacement = true) {
        override def toString: String = "Proxied pylon"
      }
    })
    var nonPylonsPlaced = 0
    buildings.filterNot(_ == Protoss.Pylon).foreach(someBuilding => {
      blueprints += new Blueprint(
        building = Some(someBuilding),
        requirePower = Some(false),
        requireCreep = Some(false),
        requireTownHallTile = Some(false),
        marginPixels = Some(0),
        respectHarvesting = Some(Tile(bestX, bestY).base.exists(b => b.owner.isUs || b.isNaturalOf.exists(_.owner.isUs))),
        requireCandidates = Some(Seq(Tile(
          bestX + walkableMargin + (if (countPylons > 0) 6 else 4) * (nonPylonsPlaced / 2),
          bestY + walkableHeight + 3 * (nonPylonsPlaced % 2)
        ))),
        forcePlacement = true) {
        override def toString: String = "Proxied " + someBuilding
      }
      nonPylonsPlaced += 1
    })

    blueprints
  }
}

class PlaceGroundProxies(buildings: UnitClass*) extends PlaceProxies(buildings: _*) {
  override val tileScore: Tile => Double = groundProxyScore
}

class PlaceAirProxies(buildings: UnitClass*) extends PlaceProxies(buildings: _*) {
  override val tileScore: Tile => Double = airProxyScore
}

class PlaceTechProxies(buildings: UnitClass*) extends PlaceProxies(buildings: _*) {
  override val tileScore: Tile => Double = techProxyScore
}