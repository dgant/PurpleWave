package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With

final class GridMobilityTerrain extends AbstractGridArray[Int] {

  override val defaultValue: Int = 0
  override val values: Array[Int] = Array.fill(width * height)(defaultValue)
  override def repr(value: Int): String = value.toString

  override def onInitialization() {

    With.grids.walkableTerrain.initialize()

    var mobility    = 0
    val horizon     = new Array[Boolean](length)
    val nextHorizon = new Array[Boolean](length)
    val explored    = new Array[Boolean](length)

    var i = 0
    while (i < length) {
      horizon(i) = ! With.grids.walkableTerrain.get(i)
      i += 1
    }

    def explore(nextIndex: Int) {
      if (nextIndex >= 0 && nextIndex < length) {
        nextHorizon(nextIndex) = ! explored(nextIndex) && ! horizon(nextIndex)
      }
    }

    var horizonSize = 1
    while (horizonSize > 0) {
      i = 0
      horizonSize = 0
      while (i < length) {
        if (horizon(i)) {
          horizonSize += 1
          values(i)   = mobility
          explored(i) = true
          explore(i - 1)
          explore(i + 1)
          explore(i - With.mapTileWidth)
          explore(i + With.mapTileWidth)
        }
        i += 1
      }

      i = 0
      while (i < length) {
        horizon(i) = nextHorizon(i)
        nextHorizon(i) = false
        i += 1
      }

      mobility += 1
    }
  }
}