package Mathematics.Shapes

import Mathematics.Points.Point

object Spiral {
  
  // Via http://stackoverflow.com/questions/3706219/algorithm-for-iterating-over-an-outward-spiral-on-a-discrete-2d-grid-from-the-or
  //
  def apply(radius: Int): IndexedSeq[Point] = {
    var dx = 1
    var dy = 0
    var segment_length = 1

    var x = 0
    var y = 0
    var segment_passed = 0

    val pointsToSearch = (2 * radius + 1) * (2 * radius + 1)
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
      Point(x, y)
    })
 }
}
