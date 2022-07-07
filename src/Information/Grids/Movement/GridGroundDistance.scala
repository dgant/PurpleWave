package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral

class GridGroundDistance(initialOrigins: Tile*) extends AbstractGridArray[Int] {

  final override val defaultValue: Int = 256 * 256
  final override val values: Array[Int] = Array.fill(length)(defaultValue)

  def origins: Seq[Tile] = initialOrigins

  @inline final def walkable(tile: Tile): Boolean = walkable(tile.i)
  @inline final def walkable(iTile: Int): Boolean = (
    With.grids.walkable.get(iTile)
    || (With.grids.walkableTerrain.get(iTile) && With.frame < 9 && With.units.ours.exists(_.tileArea.tiles.exists(_.i == iTile))))

  override def onInitialization(): Unit = {
    val seeds = Maff.orElse(origins.filter(walkable), origins.flatMap(seed => Spiral(20).map(seed.add).filter(_.valid).find(walkable)))
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
      open = if (open.eq(tilesA)) tilesB else tilesA
      openSize = 0
      for (nextTile <- 0 until nextSize) explore(next(nextTile))
      distance += 1
    }
  }
}
