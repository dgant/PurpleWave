package NeoGeo

import bwapi.Game

import scala.collection.mutable.ArrayBuffer

/**
  * NeoGeo is a reliable and easy-to-use Java-compatible terrain analyzer for the StarCraft: Brood War API (BWAPI).
  *
  * @param game
  * A JBWAPI Game object
  */
final class NeoGeo(game: Game) {
  /*
  Alternate names:
  - NeoJeo
  - HappyMappy
  - Jeography
  - Jeo
  - Jography
  - Javigator
  - Comsat
  - Jomsat
   */
  val mapFileName : String = game.mapFileName()
  val mapNickname : String = MapIdentifier(mapFileName)
  val mapHash     : String = game.mapHash()
  val tileWidth   : Int = game.mapWidth
  val tileHeight  : Int = game.mapHeight
  val tileArea    : Int = tileWidth * tileHeight
  val walkWidth   : Int = 4 * tileWidth
  val walkHeight  : Int = 4 * tileHeight
  val walkArea    : Int = walkWidth * walkHeight
  val pixelWidth  : Int = 32 * tileWidth
  val pixelHeight : Int = 32 * tileHeight
  val pixelArea   : Int = pixelWidth * pixelHeight
  def tileX(i: Int)         : Int = i % tileWidth
  def tileY(i: Int)         : Int = i / tileWidth
  def tileI(x: Int, y: Int) : Int = x + y * tileWidth
  def walkX(i: Int)         : Int = i % walkWidth
  def walkY(i: Int)         : Int = i / walkWidth
  def walkI(x: Int, y: Int) : Int = x + y * walkWidth
  def pixelX(i: Int)        : Int = i % pixelWidth
  def pixelY(i: Int)        : Int = i / pixelWidth
  val walkability     : Array[Boolean]            = Array.fill(walkArea)(false)
  val buildability    : Array[Boolean]            = Array.fill(tileArea)(false)
  val unoccupied      : Array[Boolean]            = Array.fill(tileArea)(false)
  val groundHeight    : Array[Int]                = Array.fill(tileArea)(0)
  val altitude        : Array[Int]                = Array.fill(walkArea)(0)
  val continentByTile : Array[NeoContinent]       = Array.fill(tileArea)(null)
  val metroByTile     : Array[NeoMetro]           = Array.fill(tileArea)(null)
  val regionByTile    : Array[NeoRegion]          = Array.fill(tileArea)(null)
  val baseByTile      : Array[Option[NeoBase]]    = Array.fill(tileArea)(null)
  val continents      : ArrayBuffer[NeoContinent] = ArrayBuffer.empty
  val metros          : ArrayBuffer[NeoMetro]     = ArrayBuffer.empty
  val regions         : ArrayBuffer[NeoRegion]    = ArrayBuffer.empty
  val bases           : ArrayBuffer[NeoBase]      = ArrayBuffer.empty

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
  {
    var altitudeNext  = 0
    val horizon       = Array.fill(walkArea)(false)
    val horizonNext   = Array.fill(walkArea)(false)
    val explored      = Array.fill(walkArea)(false)
    def explore(j: Int): Unit = {
      if (j >= 0 && j < walkArea) {
        horizonNext(j) = ! explored(j) && ! horizon(j)
      }
    }
    var proceed = true
    while(proceed) {
      var i = 0
      proceed = false
      while (i < walkArea) {
        if (if (altitudeNext == 0) ! walkability(i) else horizon(i)) {
          proceed = true
          explored(i) = true
          altitude(i) = altitudeNext
          explore(i - 1)
          explore(i + 1)
          explore(i - walkWidth)
          explore(i + walkWidth)
        }
        i += 1
      }
      i = 0
      while (i < walkArea) {
        horizon(i) = horizonNext(i)
        horizonNext(i) = false
        i += 1
      }
      altitudeNext += 1
    }
  }
}
