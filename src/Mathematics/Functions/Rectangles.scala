package Mathematics.Functions

trait Rectangles {
  @inline final def rectangleContains(x: Int, y: Int, x1: Int, y1: Int, x2: Int, y2: Int): Boolean = x >= x1 && x <= x2 && y >= y1 && y <= y2
  @inline final def rectanglesIntersect(ax1: Int, ay1: Int, ax2: Int, ay2: Int, bx1: Int, by1: Int, bx2: Int, by2: Int): Boolean = (
        rectangleContains(ax1, ay1, bx1, by1, bx2, by2)
    ||  rectangleContains(ax1, ay2, bx1, by1, bx2, by2)
    ||  rectangleContains(ax2, ay1, bx1, by1, bx2, by2)
    ||  rectangleContains(ax2, ay2, bx1, by1, bx2, by2)
    ||  rectangleContains(bx1, by1, ax1, ay1, ax2, ay2)
    ||  rectangleContains(bx1, by2, ax1, ay1, ax2, ay2)
    ||  rectangleContains(bx2, by1, ax1, ay1, ax2, ay2)
    ||  rectangleContains(bx2, by2, ax1, ay1, ax2, ay2)
  )
}
