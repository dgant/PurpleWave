package Information.Grids.Spells

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Points.Pixel

class GridPsionicStorm extends AbstractGridFramestamp {
  
  override protected def updateCells(): Unit = {
    With.bullets.all.foreach(bullet => addPsionicStorm(bullet.pixel))
  }
  
  def addPsionicStorm(pixel: Pixel) {
    val tile = pixel.tileIncluding
    for (dx <- Array(-1, 0, 1)) {
      for (dy <- Array(-1, 0, 1)) {
        stamp(tile.add(dx, dy))
      }
    }
  }
}
