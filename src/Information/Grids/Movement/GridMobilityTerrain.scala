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
      var rowStart = 0
      while (rowStart < length) {
        val rowEnd = rowStart + width
        while (i < rowEnd) {
          if (horizon(i)) {
            horizonSize += 1
            values(i)   = mobility
            explored(i) = true
            if (i > rowStart) explore(i - 1)
            if (i + 1 < rowEnd) explore(i + 1)
            explore(i - width)
            explore(i + width)
          }
          i += 1
        }
        rowStart = rowEnd
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
