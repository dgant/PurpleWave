package Placement.Generation

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Point, Tile, WalkTile}
import Mathematics.Shapes.Pylons
import Placement.Access.PlaceLabels.{CannonRush, DefendGround, Defensive, Important, Supply}
import Placement.Placement
import Placement.Templating.{Template, TemplatePointRequirement}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable

case class CannonRushPlacement(base: Base, cannon: Tile, pylons: Vector[Tile], fit: Fit)

object CannonRushFinder {

  private case class Box(left: Int, top: Int, right: Int, bottom: Int) {
    def center: Pixel = Pixel((left + right) / 2, (top + bottom) / 2)
  }

  private val directionX = Array(-1, 1, 0, 0, -1, 1, -1, 1)
  private val directionY = Array(0, 0, -1, 1, -1, -1, 1, 1)

  private def intersects(a: Box, b: Box): Boolean =
    a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom

  private def foundationsIntersect(a: Tile, aBuilding: UnitClass, b: Tile, bBuilding: UnitClass): Boolean =
    a.x < b.x + bBuilding.tileWidth &&
    b.x < a.x + aBuilding.tileWidth &&
    a.y < b.y + bBuilding.tileHeight &&
    b.y < a.y + aBuilding.tileHeight

  private def distance(a: Box, b: Box): Double =
    Maff.broodWarDistanceBox(a.left, a.top, a.right, a.bottom, b.left, b.top, b.right, b.bottom)

  private def buildingBox(tile: Tile, building: UnitClass): Box = {
    val center = tile.topLeftPixel.add(16 * building.tileWidth, 16 * building.tileHeight)
    Box(
      center.x - building.dimensionLeft,
      center.y - building.dimensionUp,
      center.x + building.dimensionRightExclusive,
      center.y + building.dimensionDownExclusive)
  }

  private def edgeDistance(tile: Tile, building: UnitClass): Int = Math.min(
    Math.min(tile.x, With.mapTileWidth - tile.x - building.tileWidth),
    Math.min(tile.y, With.mapTileHeight - tile.y - building.tileHeight))

  private def attackerBox(center: Pixel, attacker: UnitClass): Box = Box(
    center.x - attacker.dimensionLeft,
    center.y - attacker.dimensionUp,
    center.x + attacker.dimensionRightExclusive,
    center.y + attacker.dimensionDownExclusive)

  private class AttackGrid(attacker: UnitClass, cannon: Box, staticBlockers: Vector[Box], pylonCandidates: Vector[Tile]) {
    private val marginPixels = 32 * 10
    private val mapWalkWidth = With.mapTileWidth * 4
    private val mapWalkHeight = With.mapTileHeight * 4
    private val xMin = Math.max(0, (cannon.left - marginPixels) / 8)
    private val yMin = Math.max(0, (cannon.top - marginPixels) / 8)
    private val xMax = Math.min(mapWalkWidth - 1, (cannon.right + marginPixels) / 8)
    private val yMax = Math.min(mapWalkHeight - 1, (cannon.bottom + marginPixels) / 8)
    private val width = xMax - xMin + 1
    private val height = yMax - yMin + 1
    private val size = width * height
    private val basePassable = Array.ofDim[Boolean](size)
    private val pylonBoxes = pylonCandidates.map(buildingBox(_, Protoss.Pylon))
    private val pylonIndices = pylonCandidates.zipWithIndex.toMap
    private val blockedLow = Array.ofDim[Long](size)
    private val blockedHigh = Array.ofDim[Long](size)
    private val attackIndices = new mutable.ArrayBuffer[Int]
    private val visited = Array.ofDim[Int](size)
    private val queue = Array.ofDim[Int](size)
    private var stamp = 0

    private def index(x: Int, y: Int): Int = (x - xMin) + width * (y - yMin)
    private def xOf(i: Int): Int = xMin + i % width
    private def yOf(i: Int): Int = yMin + i / width
    private def center(x: Int, y: Int): Pixel = Pixel(8 * x + 4, 8 * y + 4)

    private def terrainPassable(box: Box): Boolean =
      box.left >= 0 &&
      box.top >= 0 &&
      box.right <= With.mapPixelWidth &&
      box.bottom <= With.mapPixelHeight &&
      With.game.isWalkable(WalkTile(box.left / 8, box.top / 8).bwapi) &&
      With.game.isWalkable(WalkTile((box.right - 1) / 8, box.top / 8).bwapi) &&
      With.game.isWalkable(WalkTile(box.left / 8, (box.bottom - 1) / 8).bwapi) &&
      With.game.isWalkable(WalkTile((box.right - 1) / 8, (box.bottom - 1) / 8).bwapi)

    private def intersectsStatic(box: Box): Boolean = {
      var blockerIndex = 0
      while (blockerIndex < staticBlockers.length) {
        if (intersects(box, staticBlockers(blockerIndex))) return true
        blockerIndex += 1
      }
      false
    }

    private var y = yMin
    while (y <= yMax) {
      var x = xMin
      while (x <= xMax) {
        val i = index(x, y)
        val box = attackerBox(center(x, y), attacker)
        basePassable(i) = terrainPassable(box) && ! intersects(box, cannon) && ! intersectsStatic(box)
        if (basePassable(i)) {
          if (distance(box, cannon) <= attacker.groundRangeRaw) attackIndices += i
          var pylonIndex = 0
          while (pylonIndex < pylonBoxes.length) {
            if (intersects(box, pylonBoxes(pylonIndex))) {
              if (pylonIndex < 64) blockedLow(i) |= 1L << pylonIndex
              else blockedHigh(i) |= 1L << (pylonIndex - 64)
            }
            pylonIndex += 1
          }
        }
        x += 1
      }
      y += 1
    }

    private def masks(pylons: Vector[Tile]): (Long, Long) = {
      var low = 0L
      var high = 0L
      var i = 0
      while (i < pylons.length) {
        val pylonIndex = pylonIndices(pylons(i))
        if (pylonIndex < 64) low |= 1L << pylonIndex
        else high |= 1L << (pylonIndex - 64)
        i += 1
      }
      (low, high)
    }

    private def border(i: Int): Boolean = {
      val x = xOf(i)
      val y = yOf(i)
      x == xMin || x == xMax || y == yMin || y == yMax
    }

    def sealedFrom(pylons: Vector[Tile]): Boolean = {
      val pylonMasks = masks(pylons)
      val pylonMaskLow = pylonMasks._1
      val pylonMaskHigh = pylonMasks._2
      def passable(i: Int): Boolean =
        basePassable(i) &&
        (blockedLow(i) & pylonMaskLow) == 0L &&
        (blockedHigh(i) & pylonMaskHigh) == 0L
      stamp += 1
      if (stamp == Int.MaxValue) {
        java.util.Arrays.fill(visited, 0)
        stamp = 1
      }
      var read = 0
      var write = 0
      var attackIndex = 0
      while (attackIndex < attackIndices.length) {
        val i = attackIndices(attackIndex)
        if (visited(i) != stamp && passable(i)) {
          if (border(i)) return false
          visited(i) = stamp
          queue(write) = i
          write += 1
        }
        attackIndex += 1
      }
      while (read < write) {
        val current = queue(read)
        read += 1
        val currentX = xOf(current)
        val currentY = yOf(current)
        var directionIndex = 0
        while (directionIndex < directionX.length) {
          val deltaX = directionX(directionIndex)
          val deltaY = directionY(directionIndex)
          val nextX = currentX + deltaX
          val nextY = currentY + deltaY
          if (nextX >= xMin && nextX <= xMax && nextY >= yMin && nextY <= yMax) {
            val next = index(nextX, nextY)
            if (visited(next) != stamp && passable(next)) {
              val diagonal = deltaX != 0 && deltaY != 0
              val canTurnCorner = ! diagonal ||
                passable(index(currentX + deltaX, currentY)) ||
                passable(index(currentX, currentY + deltaY))
              if (canTurnCorner) {
                if (border(next)) return false
                visited(next) = stamp
                queue(write) = next
                write += 1
              }
            }
          }
          directionIndex += 1
        }
      }
      true
    }
  }

  private class LazyAttackGrid(attacker: UnitClass, cannon: Box, staticBlockers: Vector[Box], pylonCandidates: Vector[Tile]) {
    lazy val value = new AttackGrid(attacker, cannon, staticBlockers, pylonCandidates)
  }

  private def makeFit(cannon: Tile, pylons: Vector[Tile]): Fit = {
    val origin = Tile((pylons :+ cannon).map(_.x).min, (pylons :+ cannon).map(_.y).min)
    val template = new Template
    pylons.foreach(pylon => template.add(
      Point(pylon.x - origin.x, pylon.y - origin.y),
      new TemplatePointRequirement(Protoss.Pylon).withLabels(CannonRush, Supply, Important)))
    template.add(
      Point(cannon.x - origin.x, cannon.y - origin.y),
      new TemplatePointRequirement(Protoss.PhotonCannon).withLabels(CannonRush, Defensive, DefendGround, Important))
    Fit(origin, template, 1)
  }

  private def behindMinerals(base: Base, cannon: Tile): Boolean = {
    if (base.minerals.isEmpty) return false
    val hall = base.townHallArea.midPixel
    val box = buildingBox(cannon, Protoss.PhotonCannon)
    val mineral = base.minerals.minBy(m => m.pixel.pixelDistanceSquared(box.center))
    val mineralBox = Box(mineral.pixelStart.x, mineral.pixelStart.y, mineral.pixelEndExclusive.x, mineral.pixelEndExclusive.y)
    val outwardX = mineral.pixel.x - hall.x
    val outwardY = mineral.pixel.y - hall.y
    val beyondX = box.center.x - mineral.pixel.x
    val beyondY = box.center.y - mineral.pixel.y
    beyondX.toLong * outwardX + beyondY.toLong * outwardY > 0 && distance(box, mineralBox) <= 32 * 3
  }

  private def cannonCandidates(base: Base, placement: Placement): Vector[Tile] = {
    val candidates = base.minerals
      .flatMap(_.tileArea.expand(5, 5).tiles)
      .filter(_.valid)
      .filter(behindMinerals(base, _))
      .distinct
      .map(tile => (tile, makeFit(tile, Vector.empty)))
      .filter(candidate => placement.fitsAt(candidate._2.template, candidate._2.origin))
      .map(_._1)
      .toVector
    candidates.sortBy(candidate => {
      val box = buildingBox(candidate, Protoss.PhotonCannon)
      val mineralDistance = base.minerals.map(mineral => distance(box, Box(
        mineral.pixelStart.x,
        mineral.pixelStart.y,
        mineral.pixelEndExclusive.x,
        mineral.pixelEndExclusive.y))).min
      (edgeDistance(candidate, Protoss.PhotonCannon), mineralDistance, candidate.tileDistanceSquared(base.heart))
    })
  }

  private def pylonCandidates(cannon: Tile, placement: Placement): Vector[Tile] = {
    val cannonBox = buildingBox(cannon, Protoss.PhotonCannon)
    val candidates = (cannon.x - 4 to cannon.x + 4).flatMap(x =>
      (cannon.y - 4 to cannon.y + 4).map(y => Tile(x, y)))
      .filter(_.valid)
      .filterNot(foundationsIntersect(_, Protoss.Pylon, cannon, Protoss.PhotonCannon))
      .filter(tile => {
        val fit = makeFit(cannon, Vector(tile))
        placement.fitsAt(fit.template, fit.origin)
      })
      .toVector
    candidates.sortBy(tile => (
      edgeDistance(tile, Protoss.Pylon),
      distance(buildingBox(tile, Protoss.Pylon), cannonBox),
      tile.tileDistanceSquared(With.geography.home)))
  }

  private def configurations(cannon: Tile, pylons: Vector[Tile]): Vector[Vector[Tile]] = {
    val cannonBox = buildingBox(cannon, Protoss.PhotonCannon)
    (pylons.map(Vector(_)) ++ pylons.combinations(2).map(_.toVector))
      .filter(configuration => configuration.combinations(2).forall(pair =>
        ! foundationsIntersect(pair.head, Protoss.Pylon, pair.last, Protoss.Pylon)))
      .filter(_.exists(Pylons.powers(_, cannon, Protoss.PhotonCannon)))
      .sortBy(configuration => (
        configuration.length,
        configuration.map(edgeDistance(_, Protoss.Pylon)).sum,
        configuration.map(tile => distance(buildingBox(tile, Protoss.Pylon), cannonBox)).max,
        configuration.map(tile => distance(buildingBox(tile, Protoss.Pylon), cannonBox)).sum))
  }

  private def isSafe(pylons: Vector[Tile], attackGrids: Vector[LazyAttackGrid]): Boolean = {
    attackGrids.forall(_.value.sealedFrom(pylons))
  }

  def find(base: Base, placement: Placement): Option[CannonRushPlacement] = {
    val started = System.nanoTime()
    if (base.minerals.isEmpty) {
      With.logger.debug(f"CANNON_RUSH_FAILURE base=${base.name} description=${base.description} heart=${base.heart} reason=no-minerals")
      return None
    }
    val staticBlockers = With.units.neutral
      .filter(unit => (unit.unitClass.isBuilding || unit.unitClass.isResource) && ! unit.flying)
      .map(unit => Box(unit.pixelStart.x, unit.pixelStart.y, unit.pixelEndExclusive.x, unit.pixelEndExclusive.y))
      .toVector
    val cannons = cannonCandidates(base, placement)
    var configurations = 0
    var cannonIndex = 0
    while (cannonIndex < cannons.length) {
      val cannon = cannons(cannonIndex)
      val pylons = pylonCandidates(cannon, placement)
      val cannonBox = buildingBox(cannon, Protoss.PhotonCannon)
      val attackGrids = Vector(Protoss.Probe, Zerg.Zergling, Terran.SCV, Zerg.Drone, Protoss.Zealot)
        .map(new LazyAttackGrid(_, cannonBox, staticBlockers, pylons))
      val iterator = this.configurations(cannon, pylons).iterator
      while (iterator.hasNext) {
        val configuration = iterator.next()
        configurations += 1
        if (isSafe(configuration, attackGrids)) {
          val orderedPylons = configuration.sortBy(_.tileDistanceSquared(With.geography.home))
          val orderedFit = makeFit(cannon, orderedPylons)
          val elapsedMs = (System.nanoTime() - started) / 1000000L
          With.logger.debug(f"CANNON_RUSH_SUCCESS base=${base.name} description=${base.description} heart=${base.heart} cannon=$cannon pylons=${orderedPylons.mkString(",")} candidates=${cannons.length} configurations=$configurations milliseconds=$elapsedMs")
          return Some(CannonRushPlacement(base, cannon, orderedPylons, orderedFit))
        }
      }
      cannonIndex += 1
    }
    val elapsedMs = (System.nanoTime() - started) / 1000000L
    With.logger.debug(f"CANNON_RUSH_FAILURE base=${base.name} description=${base.description} heart=${base.heart} candidates=${cannons.length} configurations=$configurations milliseconds=$elapsedMs")
    None
  }
}
