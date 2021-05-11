package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridInt
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral

class GridGroundDistance(initialOrigins: Tile*) extends AbstractGridInt {

  def origins: Seq[Tile] = initialOrigins

  @inline final def walkable(tile: Tile): Boolean = { tile.valid && (With.grids.walkable.get(tile) || (With.frame < 9 && With.units.ours.exists(_.tileArea.contains(tile)))) }
  @inline final def walkable(iTile: Int): Boolean = { (With.grids.walkable.get(iTile) || (With.frame < 9 && With.units.ours.exists(_.tileArea.tiles.exists(_ == iTile)))) }

  override def onInitialization(): Unit = {
    var seeds = origins.filter(walkable)
    if (seeds.isEmpty) {
      seeds = seeds.flatMap(seed => Spiral.points(20).map(seed.add).find(walkable))
    }

    var distance = 0
    var openSize = 0
    val tilesA = new Array[Int](tiles.size * 100)
    val tilesB = new Array[Int](tiles.size * 100)
    var open = tilesA
    val width = With.mapTileWidth
    def expand(iTile: Int): Unit = {
      if (iTile % width > 0) {
        open(openSize) = iTile - 1
        openSize += 1
      }
      if (iTile % width < width - 1) {
        open(openSize) = iTile + 1
        openSize += 1
      }
      if (iTile >= width) {
        open(openSize) = iTile - width
        openSize+= 1
      }
      if (iTile + width < tiles.size) {
        open(openSize) = iTile + width
        openSize+= 1
      }
    }
    def explore(iTile: Int): Unit = {
      if (get(iTile) == defaultValue && valid(iTile) && walkable(iTile)) {
        set(iTile, distance)
        expand(iTile)
      }
    }

    seeds.foreach(seed => explore(seed.i))
    distance = 1
    while(openSize > 0) {
      val next = open
      val nextSize = openSize
      open = if (open.equals(tilesA)) tilesB else tilesA
      openSize = 0
      for (nextTile <- 0 until nextSize) explore(next(nextTile))
      distance += 1
    }
  }
  override def defaultValue: Int = Int.MaxValue
}
