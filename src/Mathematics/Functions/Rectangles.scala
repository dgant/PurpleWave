package Mathematics.Functions

trait Rectangles {
  /**
    * Does the rectangle (with inclusive start and exclusive end) contain the point?
    */
  @inline final def rectangleContains(x: Int, y: Int, x1: Int, y1: Int, x2: Int, y2: Int): Boolean = x >= x1 && x < x2 && y >= y1 && y < y2

  /**
    * Do the rectangles (with inclusive start and exclusive end) intersect?
    */
  @inline final def rectanglesIntersect(ax1: Int, ay1: Int, ax2: Int, ay2: Int, bx1: Int, by1: Int, bx2: Int, by2: Int): Boolean = (
        ax1 < ax2 && ay1 < ay2 && bx1 < bx2 && by1 < by2
    &&  ax1 < bx2 && bx1 < ax2 && ay1 < by2 && by1 < ay2
  )
}
