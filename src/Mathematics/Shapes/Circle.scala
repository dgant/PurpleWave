package Mathematics.Shapes

import Mathematics.Points.Point

import scala.collection.mutable

object Circle {
  
  val points = new mutable.HashMap[Int, Vector[Point]] {
    override def default(radius: Int): Vector[Point] = {
      put(
        radius,
        (-radius to radius).flatten(x =>
          (-radius to radius).map(y =>
            (x, y, x * x + y * y < radius * radius)
          ))
          .filter(_._3)
          .map(point => new Point(point._1, point._2))
          .toVector)
      this (radius)
    }
  }
}
