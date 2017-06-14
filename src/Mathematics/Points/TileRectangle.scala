package Mathematics.Points

case class TileRectangle(
  startInclusive : Tile,
  endExclusive   : Tile) {
  
  if (endExclusive.x < startInclusive.x || endExclusive.y < startInclusive.y) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def add(x:Int, y:Int):TileRectangle =
    TileRectangle(
      startInclusive.add(x, y),
      endExclusive.add(x, y))
  
  def expand(x:Int, y:Int):TileRectangle =
    TileRectangle(
      startInclusive.add(-x, -y),
      endExclusive  .add( x,  y))
  
  def add(Tile:Tile):TileRectangle =
    add(Tile.x, Tile.y)

  lazy val midPixel : Pixel = startPixel.midpoint(endPixel)
  lazy val midpoint : Tile  = startInclusive.midpoint(endExclusive)
  
  def contains(x:Int, y:Int):Boolean =
    x >= startInclusive.x &&
    y >= startInclusive.y &&
    x < endExclusive.x &&
    y < endExclusive.y
  
  def contains(point:Tile):Boolean = {
    contains(point.x, point.y)
  }
  
  def intersects(otherRectangle: TileRectangle):Boolean = {
    containsRectangle(otherRectangle) || otherRectangle.containsRectangle(this)
  }
  
  private def containsRectangle(otherRectangle:TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive)                                       ||
    contains(otherRectangle.endExclusive.subtract(1, 1))                          ||
    contains(otherRectangle.startInclusive.x, otherRectangle.endExclusive.y - 1)  ||
    contains(otherRectangle.endExclusive.x - 1, otherRectangle.startInclusive.y)
  }
  
  lazy val startPixel : Pixel = startInclusive.topLeftPixel
  lazy val endPixel   : Pixel = endExclusive.topLeftPixel.subtract(1, 1)
  
  lazy val tiles: Array[Tile] = {
    // Scala while-loops are way faster than for-loops because they don't create Range objects
    val startX = startInclusive.x
    val startY = startInclusive.y
    val sizeX = endExclusive.x - startX
    val sizeY = endExclusive.y - startY
    val output = new Array[Tile](sizeX * sizeY)
    var x = 0
    while (x < sizeX) {
      var y = 0
      while (y < sizeY) {
        output(x + sizeX * y) = Tile(startX + x, startY + y)
        y += 1
      }
      x += 1
    }
    output
  }
}
