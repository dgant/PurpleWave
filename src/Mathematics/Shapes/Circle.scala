package Mathematics.Shapes

import Mathematics.Positions.Point

import scala.collection.mutable

object Circle {
  
  val points = new mutable.HashMap[Int, Iterable[Point]] {
    override def default(radius: Int): Iterable[Point] = {
      put(radius,
        (-radius to radius).flatten(x =>
          (-radius to radius).map(y =>
            (x, y, x * x + y * y <= radius * radius)
          ))
          .filter(_._3)
          .map(point => new Point(point._1, point._2)))
      this (radius)
    }
  }
}
