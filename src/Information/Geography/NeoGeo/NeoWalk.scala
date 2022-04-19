package Information.Geography.NeoGeo

import bwapi.WalkPosition

case class NeoWalk(x: Int, y: Int) {
  def this(walkPosition: WalkPosition) {
    this(walkPosition.getX, walkPosition.getY)
  }
}
