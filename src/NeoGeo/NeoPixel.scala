package NeoGeo

import bwapi.Position

case class NeoPixel(x: Int, y: Int) {
  def this(position: Position) {
    this(position.getX, position.getY)
  }
}
