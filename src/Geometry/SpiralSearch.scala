package Geometry

import bwapi.TilePosition

object SpiralSearch {
  
  //Via http://stackoverflow.com/questions/3706219/algorithm-for-iterating-over-an-outward-spiral-on-a-discrete-2d-grid-from-the-or
  
  def forPointsInSpiral[T](
    position:TilePosition,
    searchRadius:Integer = 20):Iterable[TilePosition] = {
    
    var dx = 1
    var dy = 0
    var segment_length = 1
    
    var x = position.getX
    var y = position.getY
    var segment_passed = 0
    
    val pointsToSearch = (2 * searchRadius + 1) * (2 * searchRadius + 1)
    (0 to pointsToSearch).map(i => {
      x += dx
      y += dy
      segment_passed += 1
      if (segment_passed == segment_length) {
        segment_passed = 0
        val swap = dx
        dx = -dy
        dy = swap
        if (dy == 0) {
          segment_length += 1
        }
      }
      new TilePosition(x, y)
    })
  }
}
