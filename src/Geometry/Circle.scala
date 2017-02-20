package Geometry

import scala.collection.mutable

object Circle {
  //Get the points (specified in dx, dy) that fall inside a radius
  val points = new mutable.HashMap[Int, Iterable[(Int, Int)]] {
    override def default(radius:Int):Iterable[(Int, Int)] = {
      (-radius to radius).flatten(x =>
        (-radius to radius).map(y =>
          (x, y, x*x+y*y<=radius*radius)
        ))
      .filter(_._3)
      .map(t => (t._1, t._2))
    }
  }
}
