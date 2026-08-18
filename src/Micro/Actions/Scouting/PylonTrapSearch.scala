package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Architecture.ArchitecturalAssessment
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

object PylonTrapSearch {

  case class Placement(tile: Tile, canBePylon: Boolean, val canBeBattery: Boolean)

  def apply(target: UnitInfo): Option[Seq[Placement]] = {
    if (target.flying || ! target.isAny(Protoss.Gateway, Terran.Factory)) {
      return None
    }

    locations(
      target.tileTopLeft,
      target.unitClass.tileWidth,
      target.unitClass.tileHeight,
      With.mapTileWidth,
      With.mapTileHeight,
      (x, y) => With.grids.walkable.getUnchecked(x + With.mapTileWidth * y),
      tile => With.architecture.assess(tile, Protoss.Pylon) == ArchitecturalAssessment.Accepted)
  }

  private[Scouting] def locations(
    targetTile    : Tile,
    targetWidth   : Int,
    targetHeight  : Int,
    mapWidth      : Int,
    mapHeight     : Int,
    tileWalkable: (Int, Int) => Boolean,
    buildingBuildable: Tile => Boolean)
  : Option[Seq[Placement]] = {
    val ring = emergenceRing(targetTile, targetWidth, targetHeight)
    val candidatesSeen = new mutable.HashSet[(Int, Int)]

    // Search likely blockers near the emergence ring first.
    val candidatesNearRing = ring
      .flatMap(tile =>
        for {
          dx <- -2 to 1
          dy <- -2 to 1
        } yield tile.add(dx, dy))
      .filter(tile =>
        tile.x >= 0 &&
        tile.y >= 0 &&
        tile.x + 1 < mapWidth &&
        tile.y + 1 < mapHeight &&
        candidatesSeen.add((tile.x, tile.y)))
      .map(Candidate)
      .filter(candidate => buildingBuildable(candidate.tile))
    val candidateTiles = candidatesNearRing ++
      (for {
        x <- targetTile.x - 4 to targetTile.x + targetWidth + 2
        y <- targetTile.y - 4 to targetTile.y + targetHeight + 2
        if
          x >= 0 &&
          y >= 0 &&
          x + 1 < mapWidth &&
          y + 1 < mapHeight &&
          candidatesSeen.add((x, y))
        candidate = Candidate(Tile(x, y))
        if buildingBuildable(candidate.tile)
      } yield candidate).toVector

    val margin  = 8
    val minX    = Math.max(0, targetTile.x - margin)
    val minY    = Math.max(0, targetTile.y - margin)
    val maxX    = Math.min(mapWidth  - 1, targetTile.x + targetWidth  + margin)
    val maxY    = Math.min(mapHeight - 1, targetTile.y + targetHeight + margin)
    val width   = maxX - minX + 1
    val height  = maxY - minY + 1
    val walkable = Array.ofDim[Boolean](width * height)
    val visited = Array.fill(width * height)(0)
    val previous = Array.fill(width * height)(-1)
    val blocked = Array.fill(width * height)(false)
    val onEscapePath = Array.fill(width * height)(0)
    val queueX  = new Array[Int](visited.length)
    val queueY  = new Array[Int](visited.length)
    var visit   = 0
    var pathVisit = 0

    var x = minX
    while (x <= maxX) {
      var y = minY
      while (y <= maxY) {
        walkable(x - minX + width * (y - minY)) = tileWalkable(x, y)
        y += 1
      }
      x += 1
    }

    def localIndex(x: Int, y: Int): Int = x - minX + width * (y - minY)

    def isWalkable(x: Int, y: Int): Boolean = {
      x >= minX &&
      y >= minY &&
      x <= maxX &&
      y <= maxY && {
        val i = localIndex(x, y)
        walkable(i) && ! blocked(i)
      }
    }

    def setBlocked(candidate: Candidate, value: Boolean): Unit = {
      val i = localIndex(candidate.tile.x, candidate.tile.y)
      blocked(i) = value
      blocked(i + 1) = value
      blocked(i + width) = value
      blocked(i + width + 1) = value
    }

    // If an escape path exists, mark one of them. Any completed trap must block
    // at least one tile on this path, which lets the combination search branch
    // only on relevant buildings instead of enumerating every candidate tuple.
    def findEscapePath(): Boolean = {
      val spawn = ring.find(tile => isWalkable(tile.x, tile.y))
      if (spawn.isEmpty) {
        return false
      }

      visit += 1
      var queueStart = 0
      var queueEnd = 1
      val spawnTile = spawn.get
      queueX(0) = spawnTile.x
      queueY(0) = spawnTile.y
      val spawnI = localIndex(spawnTile.x, spawnTile.y)
      visited(spawnI) = visit
      previous(spawnI) = -1

      while (queueStart < queueEnd) {
        val x = queueX(queueStart)
        val y = queueY(queueStart)
        val i = localIndex(x, y)
        queueStart += 1

        if (
          (x == minX && minX > 0) ||
          (y == minY && minY > 0) ||
          (x == maxX && maxX < mapWidth  - 1) ||
          (y == maxY && maxY < mapHeight - 1)) {
          pathVisit += 1
          var pathI = i
          while (pathI >= 0) {
            onEscapePath(pathI) = pathVisit
            pathI = previous(pathI)
          }
          return true
        }

        // Cardinal connectivity prevents movement through cater-corner buildings.
        var direction = 0
        while (direction < 4) {
          val nextX = x + (if (direction == 0) 1 else if (direction == 1) -1 else 0)
          val nextY = y + (if (direction == 2) 1 else if (direction == 3) -1 else 0)
          if (
            nextX >= minX &&
            nextY >= minY &&
            nextX <= maxX &&
            nextY <= maxY &&
            isWalkable(nextX, nextY)) {
            val nextI = localIndex(nextX, nextY)
            if (visited(nextI) != visit) {
              visited(nextI) = visit
              previous(nextI) = i
              queueX(queueEnd) = nextX
              queueY(queueEnd) = nextY
              queueEnd += 1
            }
          }
          direction += 1
        }
      }
      false
    }

    def intersectsEscapePath(candidate: Candidate): Boolean = {
      val i = localIndex(candidate.tile.x, candidate.tile.y)
      onEscapePath(i) == pathVisit ||
      onEscapePath(i + 1) == pathVisit ||
      onEscapePath(i + width) == pathVisit ||
      onEscapePath(i + width + 1) == pathVisit
    }

    def find(count: Int, candidates: Vector[Candidate]): Option[Vector[Candidate]] = {
      val selected = new Array[Candidate](count)
      val selectedIndices = new Array[Int](count)
      val candidatesOnPath = Array.fill(count)(new Array[Int](candidates.length))
      val statesSeen = new mutable.HashSet[Long]
      val stateBase = candidates.length + 1L

      def stateKey(depth: Int): Long = {
        if (depth == 0) {
          return 0L
        }
        var a = selectedIndices(0)
        var b = if (depth > 1) selectedIndices(1) else -1
        var c = if (depth > 2) selectedIndices(2) else -1
        if (b >= 0 && a > b) { val swap = a; a = b; b = swap }
        if (c >= 0 && b > c) { val swap = b; b = c; c = swap }
        if (b >= 0 && a > b) { val swap = a; a = b; b = swap }
        var output = depth.toLong
        output = output * stateBase + a + 1
        if (b >= 0) output = output * stateBase + b + 1
        if (c >= 0) output = output * stateBase + c + 1
        output
      }

      def recurse(depth: Int): Option[Vector[Candidate]] = {
        if ( ! statesSeen.add(stateKey(depth))) {
          return None
        }
        if ( ! findEscapePath()) {
          return Some(selected.take(depth).toVector)
        }
        if (depth == count) {
          return None
        }

        val pathCandidates = candidatesOnPath(depth)
        var pathCandidateCount = 0
        var i = 0
        while (i < candidates.length) {
          val candidate = candidates(i)
          var unavailable = intersectsEscapePath(candidate)
          var selectedI = 0
          while (selectedI < depth && unavailable) {
            unavailable = selectedIndices(selectedI) != i && ! candidate.overlaps(selected(selectedI))
            selectedI += 1
          }
          if (unavailable) {
            pathCandidates(pathCandidateCount) = i
            pathCandidateCount += 1
          }
          i += 1
        }

        var pathCandidateI = 0
        while (pathCandidateI < pathCandidateCount) {
          i = pathCandidates(pathCandidateI)
          val candidate = candidates(i)
          var overlaps = false
          var selectedI = 0
          while (selectedI < depth && ! overlaps) {
            overlaps = selectedIndices(selectedI) == i || candidate.overlaps(selected(selectedI))
            selectedI += 1
          }
          if ( ! overlaps) {
            selected(depth) = candidate
            selectedIndices(depth) = i
            setBlocked(candidate, value = true)
            val output = recurse(depth + 1)
            setBlocked(candidate, value = false)
            if (output.isDefined) {
              return output
            }
          }
          pathCandidateI += 1
        }
        None
      }

      recurse(0)
    }

    val output = (0 to 2)
      .view
      .flatMap(count => find(count, candidateTiles))
      .headOption
      .orElse(find(3, candidatesNearRing))
      .orElse(find(3, candidateTiles))
    output
      .map(_.map(candidate => Placement(candidate.tile, canBePylon = true, canBeBattery = true)))
  }

  private case class Candidate(tile: Tile) {
    def contains(x: Int, y: Int): Boolean = {
      x >= tile.x && x < tile.x + 2 && y >= tile.y && y < tile.y + 2
    }

    def overlaps(other: Candidate): Boolean = {
      tile.x < other.tile.x + 2 &&
      other.tile.x < tile.x + 2 &&
      tile.y < other.tile.y + 2 &&
      other.tile.y < tile.y + 2
    }
  }

  private def emergenceRing(tile: Tile, width: Int, height: Int): Vector[Tile] = {
    (
      (0 until width).map(x => tile.add(x, height + 1)) ++
      (height to 0 by -1).map(y => tile.add(width, y)) ++
      (width - 1 to 0 by -1).map(x => tile.add(x, -1)) ++
      (0 to height).map(y => tile.add(-1, y)))
      .toVector
  }

}
