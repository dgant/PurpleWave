package Information.Grids.Spells

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With
import Mathematics.Points.Pixel

class GridPsionicStorm extends AbstractGridTimestamp {
  
  override protected def updateTimestamps(): Unit = {
    With.bullets.all.foreach(bullet => addPsionicStorm(bullet.pixel))
  }
  
  def addPsionicStorm(pixel: Pixel) {
    val tile = pixel.tileIncluding
    for (dx <- Array(-1, 0, 1)) {
      for (dy <- Array(-1, 0, 1)) {
        set(tile.add(dx, dy), With.frame)
      }
    }
  }
}
