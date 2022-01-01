package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArray

class GridMapMargin extends AbstractGridArray[Int] {
  override val defaultValue: Int = 0
  override protected val values: Array[Int] = Array.fill(width * height)(defaultValue)

  override def onInitialization(): Unit = {
    var i = 0
    while (i < length) {
      val x = i % width
      val y = i / width
      values(i) = Array(x, y, width - x, height - y).min
      i += 1
    }
  }
}
