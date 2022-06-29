package Information.Geography.NeoGeo

import Information.Geography.NeoGeo.Internal.{NeoColors, NeoContinentBackend}
import bwapi.Game

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Sorting

/**
  * NeoGeo is a reliable and easy-to-use Java-compatible terrain analyzer for the StarCraft: Brood War API (BWAPI).
  *
  * @param game
  * A JBWAPI Game object
  */
final class NeoGeo(game: Game) {
  @inline def tileX(i: Int)         : Int = i % tileWidth
  @inline def tileY(i: Int)         : Int = i / tileWidth
  @inline def tileI(x: Int, y: Int) : Int = x + y * tileWidth
  @inline def tileI(xy: (Int, Int)) : Int = tileI(xy._1, xy._2)
  @inline def walkX(i: Int)         : Int = i % walkWidth
  @inline def walkY(i: Int)         : Int = i / walkWidth
  @inline def walkI(x: Int, y: Int) : Int = x + y * walkWidth
  @inline def walkI(xy: (Int, Int)) : Int = walkI(xy._1, xy._2)
  @inline def pixelX(i: Int)        : Int = i % pixelWidth
  @inline def pixelY(i: Int)        : Int = i / pixelWidth
  @inline def isValidWalk(walkI: Int): Boolean = walkI >= 0 && walkI < walkArea
  @inline def isValidWalk(walkX: Int, walkY: Int): Boolean = walkX >= 0 && walkY >= 0 && walkX < walkWidth && walkY < walkHeight
  @inline def isValidWalk(walk: (Int, Int)): Boolean = isValidWalk(walk._1, walk._2)
  @inline def isWalkableUnsafe(walkI: Int): Boolean = walkability(walkI)
  @inline def isWalkableUnsafe(walkX: Int, walkY: Int): Boolean = isWalkableUnsafe(walkI(walkX, walkY))
  @inline def isWalkableUnsafe(walk: (Int, Int)): Boolean = isWalkableUnsafe(walk._1, walk._2)
  @inline def isWalkable(walkI: Int): Boolean = isValidWalk(walkI) && isWalkableUnsafe(walkI)
  @inline def isWalkable(walkX: Int, walkY: Int): Boolean = isValidWalk(walkX, walkY) && isWalkableUnsafe(walkX, walkY)
  @inline def isWalkable(walk: (Int, Int)): Boolean = isWalkable(walk._1, walk._2)
  @inline def adjacentWalks4(walkI: Int): Array[Int] = Array(walkI - walkWidth, walkI - 1, walkI + 1, walkI + walkWidth)
  @inline def adjacentWalks4(walkX: Int, walkY: Int): Array[(Int, Int)] = Array((walkX, walkY - 1), (walkX - 1, walkY), (walkX + 1, walkY), (walkX, walkY + 1))
  @inline def adjacentWalks4(walk: (Int, Int)): Array[(Int, Int)] = adjacentWalks4(walk._1, walk._2)

  val directions: Array[(Double, Double)] = Array((1, 0), (1, 0.5), (1, 1), (0.5, 1), (0, 1), (-0.5, 1), (-1, 1), (-1, 0.5), (-1, 0), (-1, -0.5), (-1, -1), (-0.5, -1), (0, -1), (0.5, -1), (1, -1), (1, -0.5))
  val mapFileName : String = game.mapFileName()
  val mapNickname : String = MapIdentifier(mapFileName)
  val mapHash     : String = game.mapHash()
  val tileWidth   : Int = game.mapWidth
  val tileHeight  : Int = game.mapHeight
  val tileMaxDim  : Int = Math.max(tileWidth, tileHeight)
  val tileArea    : Int = tileWidth * tileHeight
  val walkWidth   : Int = 4 * tileWidth
  val walkHeight  : Int = 4 * tileHeight
  val walkMaxDim  : Int = Math.max(walkWidth, walkHeight)
  val walkArea    : Int = walkWidth * walkHeight
  val pixelWidth  : Int = 32 * tileWidth
  val pixelHeight : Int = 32 * tileHeight
  val pixelMaxDim : Int = Math.max(pixelWidth, pixelHeight)
  val pixelArea   : Int = pixelWidth * pixelHeight
  val walkability       : Array[Boolean]            = Array.fill(walkArea)(false)
  val buildability      : Array[Boolean]            = Array.fill(tileArea)(false)
  val unoccupied        : Array[Boolean]            = Array.fill(tileArea)(false)
  val groundHeight      : Array[Int]                = Array.fill(tileArea)(0)
  val altitude          : Array[Double]             = Array.fill(walkArea)(walkMaxDim)
  val clearance         : Array[Array[Int]]         = directions.map(x => Array.fill(walkArea)(0))
  val clearanceMinDir   : Array[Int]                = Array.fill(walkArea)(0)
  val clearanceMaxDir   : Array[Int]                = Array.fill(walkArea)(0)
  val clearanceMinWalks : Array[Int]                = Array.fill(walkArea)(0)
  val clearanceMaxWalks : Array[Int]                = Array.fill(walkArea)(0)
  val continentByWalk   : Array[NeoContinent]       = Array.fill(walkArea)(null)
  val metroByWalk       : Array[NeoMetro]           = Array.fill(walkArea)(null)
  val regionByWalk      : Array[NeoRegion]          = Array.fill(walkArea)(null)
  val baseByWalk        : Array[Option[NeoBase]]    = Array.fill(walkArea)(null)
  val continents        : ArrayBuffer[NeoContinent] = ArrayBuffer.empty
  val metros            : ArrayBuffer[NeoMetro]     = ArrayBuffer.empty
  val regions           : ArrayBuffer[NeoRegion]    = ArrayBuffer.empty
  val bases             : ArrayBuffer[NeoBase]      = ArrayBuffer.empty
  val chokes            : ArrayBuffer[NeoRegion]    = ArrayBuffer.empty

  // Populate walk grids
  {
    var x = 0
    var y = 0
    var i = 0
    while (y < walkHeight) {
      while (x < walkWidth) {
        i = x + y * walkWidth
        walkability(i) = game.isWalkable(x, y)
        x += 1
        i += 1
      }
      x = 0
      y += 1
    }
  }
  // Populate tile grids
  {
    var x = 0
    var y = 0
    var i = 0
    while (y < tileHeight) {
      while (x < tileWidth) {
        buildability(i) = game.isBuildable(x, y)
        groundHeight(i) = game.getGroundHeight(x, y)
        x += 1
        i += 1
      }
      x = 0
      y += 1
    }
  }

  // Populate altitude
  private val walkCoast: Array[(Int, Int)] = (-1 to walkWidth).flatMap(dx => (-1 to walkHeight).map((dx, _))).filterNot(isWalkable).filter(adjacentWalks4(_).exists(isWalkable)).toArray
  private val distances: Array[((Int, Int), Double)] = (0 until walkMaxDim / 2  + 3).flatMap(dx => (dx until walkMaxDim / 2  + 3).map((dx, _))).map(p => (p, NeoMath.lengthBW(p))).toArray
  Sorting.quickSort(distances)(Ordering.by(_._2))
  @inline private def populateAltitudeAt(walkX: Int, walkY: Int, distance: Double): Unit = {
    if (isValidWalk(walkX, walkY)) {
      val i = walkI(walkX, walkY)
      if (distance < altitude(i)) {
        altitude(i) = distance
      }
    }
  }
  @inline private def populateAltitudeFrom(coastX: Int, coastY: Int, da: Int, db: Int, distance: Double): Unit = {
    populateAltitudeAt(coastX + da, coastY + db, distance)
    populateAltitudeAt(coastX - da, coastY + db, distance)
    populateAltitudeAt(coastX + da, coastY - db, distance)
    populateAltitudeAt(coastX - da, coastY - db, distance)
    populateAltitudeAt(coastX + db, coastY + da, distance)
    populateAltitudeAt(coastX - db, coastY + da, distance)
    populateAltitudeAt(coastX + db, coastY - da, distance)
    populateAltitudeAt(coastX - db, coastY - da, distance)
  }
  {
    var i = 0
    while (i < distances.length) {
      var j = 0
      while (j < walkCoast.length) {
        populateAltitudeFrom(walkCoast(j)._1, walkCoast(j)._2, distances(i)._1._1, distances(i)._1._2, distances(i)._2)
        j += 1
      }
      i += 1
    }
  }

  // Populate clearance
  private def populateClearance(values: Array[Int], sx: Int, sy: Int, dx: Double, dy: Double): Unit = {
    val zMax = walkMaxDim / Math.min(Math.abs(dx), Math.abs(dy))
    var contiguous = 0
    var z = 0
    while (z < zMax) {
      val x = sx + (dx * z).toInt
      val y = sy + (dy * z).toInt
      if (x < 0 || y < 0 || x >= walkWidth || y >= walkHeight) return
      val i = walkI(x, y)
      if (walkability(i)) {
        contiguous += 1
        values(i) = contiguous
      } else {
        contiguous = 0
      }
      z += 1
    }
  }
  {
    // For each direction tuple, iterate over walktiles from the map edge.
    // Trace a line and count contiguous walkable tiles in that direction.
    // For each tile, set its clearance in the opposite direction to be the number
    // of contiguous walkable tiles thusfar.
    directions.indices.foreach(directionIndex => {
      val clearanceIndex = (directionIndex + directions.length / 2) % directions.length
      val clearanceArray = clearance(clearanceIndex)
      val dx: Double = directions(directionIndex)._1
      val dy: Double = directions(directionIndex)._2
      if (dx != 0) {
        var sx = if (dx > 0) 0 else walkWidth - 1
        var sy = 0
        while (sy < walkHeight) {
          populateClearance(clearanceArray, sx, sy, dx, dy)
          sy += 1
        }
      }
      if (dy != 0) {
        var sy = if (dy > 0) 0 else walkHeight - 1
        var sx = 0
        while (sx < walkWidth) {
          populateClearance(clearanceArray, sx, sy, dx, dy)
          sx += 1
        }
      }
    })
  }
  // Populate minimum/maximum cross-sectional clearance
  {
    val axes = directions.length / 2
    var i = 0
    while (i < walkArea) {
      if (walkability(i)) {
        var j = 0
        var minWalks = walkArea
        var minDir = 0
        var maxWalks = - 1
        var maxDir = 0
        while (j < axes) {
          val axisWidth = clearance(j)(i) + clearance(j + axes)(i)
          if (axisWidth < minWalks) {
            minWalks = axisWidth
            minDir = j
          }
          if (axisWidth > maxWalks) {
            maxWalks = axisWidth
            maxDir = j
          }
          j += 1
        }
        clearanceMinWalks(i) = minWalks
        clearanceMinDir(i) = minDir
        clearanceMaxWalks(i) = maxWalks
        clearanceMaxDir(i) = maxDir
      }
      i += 1
    }
  }

  // Populate continents
  {
    var i = 0
    val horizon = new mutable.Queue[Int]
    while (i < walkArea) {
      if (walkability(i) && continentByWalk(i) == null) {
        horizon.enqueue(i)
        val continent = new NeoContinentBackend
        continent.color = NeoColors.allContrasted(continents.size % NeoColors.allContrasted.length)
        continents += continent
        while (horizon.nonEmpty) {
          val nextI = horizon.dequeue() // Intentionally reusing name to avoid reaching out of scope
          if (isWalkable(nextI) && continentByWalk(nextI) == null) {
            continent.walks += nextI
            continentByWalk(nextI) = continent
            horizon ++= adjacentWalks4(nextI)
          }
        }
      }
      i += 1
    }
  }

  {
    val minima = new mutable.Queue[Int]
    continents.foreach(continent => {
      minima.clear()
      var j = 0
      while (j < continent.walkable.size) {
        val i = continent.walkable(j)
        val clearance = clearanceMinWalks(i)
        if ( ! adjacentWalks4(i).exists(a => isValidWalk(a) && clearanceMinWalks(a) < clearance)) {
          minima += j
        }
      }
      //val chokeTiles =
    })
  }
}
