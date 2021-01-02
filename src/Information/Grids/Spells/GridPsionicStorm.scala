package Information.Grids.Spells

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Points.Pixel
import bwapi.BulletType

class GridPsionicStorm extends AbstractGridFramestamp {
  
  override protected def updateCells(): Unit = {
    With.bullets.all
      .view
      .filter(_.bulletType == BulletType.Psionic_Storm)
      .foreach(bullet => addPsionicStorm(bullet.pixel))
  }
  
  def addPsionicStorm(pixel: Pixel) {
    val tile = pixel.tile
    for (dx <- Array(-1, 0, 1)) {
      for (dy <- Array(-1, 0, 1)) {
        stamp(tile.add(dx, dy))
      }
    }
  }
}
