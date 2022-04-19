package Information.Geography.NeoGeo

import bwapi.TilePosition

case class NeoTile(x: Int, y: Int) {
  def this(tilePosition: TilePosition) {
    this(tilePosition.getX, tilePosition.getY)
  }
}
