package Mathematics.Shapes

import Mathematics.Points.{AbstractPoint, Point}

object Arc {
  def apply(
     from: AbstractPoint,
     to: AbstractPoint,
     innerRadius: Double,
     stepDistance: Double,
     arcsOutward: Int = 1,
     maxDeltaRadius: Double = Math.PI)
      : Seq[Point] = {

    val centerRadians = Math.atan2(to.y - from.y, to.x - from.x)

    (0 until arcsOutward).view.flatMap(arcIndex =>
    {
      val arcRadius = innerRadius + arcIndex * stepDistance
      (0 until (2 * maxDeltaRadius * arcRadius / stepDistance).toInt).view.map(stepIndex => {
        val slot = (stepIndex + 1) / 2 * ((stepIndex & 1) * 2 - 1)
        val radians = centerRadians + stepDistance * slot
        Point(
          (arcRadius * Math.cos(radians)).toInt,
          (arcRadius * Math.sin(radians)).toInt)
      })
    })
  }
}
