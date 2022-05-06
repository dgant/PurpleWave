package Mathematics.Functions

import Mathematics.Maff.signum
import Mathematics.Points.{AbstractPoint, Pixel}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Polygons {

  @inline final def distanceFromLineSegment(point: AbstractPoint, start: AbstractPoint, end: AbstractPoint): Double = {
    val x             = point.x
    val y             = point.y
    val x0            = start.x
    val y0            = start.y
    val x1            = end.x
    val y1            = end.y
    val dx0           = x - x0
    val dy0           = y - y0
    val dx1           = x1 - x0
    val dy1           = y1 - y0
    val dotProduct    = dx0 * dx1 + dy0 * dy1
    val lengthSquared = dx1 * dx1 + dy1 * dy1
    val param         = if (lengthSquared != 0.0) dotProduct / lengthSquared else -1.0

    var xx = 0.0
    var yy = 0.0
    if (param < 0) {
      xx = x0
      yy = y0
    } else if (param > 1) {
      xx = x1
      yy = y1
    } else {
      xx = x0 + param * dx1
      yy = y0 + param * dy1
    }
    val dx = x - xx
    val dy = y - yy
    Math.sqrt(dx * dx + dy * dy)
  }

  /**
    * @return 0 if colinear, 1 if clockwise, -1 if counterclockwise
    */
  @inline final def clockDirection(a: AbstractPoint, b: AbstractPoint, c: AbstractPoint): Int = {
    // See https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order/1180256#1180256
    // and https://www.geeksforgeeks.org/orientation-3-ordered-points/
    signum((b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y))
  }

  @inline final def convexHull(points: Seq[Pixel]): Seq[Pixel] = convexHull(points, (pixel: Pixel) => pixel)
  final def convexHull[T](points: Seq[T], extract: T => Pixel): Seq[T] = {
    // See https://en.wikipedia.org/wiki/Graham_scan
    if (points.size <= 2) return points

    // Find an extremum, guaranteed to be on the hull
    val yMax = points.view.map(extract(_).y).max
    val origin = points.view.filter(extract(_).y == yMax).maxBy(extract(_).x)

    // Get points sorted by polar angle with the origin, discarding closer points
    var sortedPoints = new ArrayBuffer[(T, Double)]
    sortedPoints ++= points.view.map(p => (p, extract(origin).radiansTo(extract(p))))
    sortedPoints = sortedPoints.sortBy(_._2).sortBy(_._1 != origin)
    var i: Int = 1
    while(i < sortedPoints.length) {
      val a = sortedPoints(i - 1)
      val b = sortedPoints(i)
      if (a._2 == b._2) {
        sortedPoints.remove(if (extract(origin).pixelDistanceSquared(extract(a._1)) < extract(origin).pixelDistanceSquared(extract(b._1))) i - 1 else i)
      } else {
        i += 1
      }
    }
    val output = new mutable.Stack[T]
    sortedPoints.foreach(point => {
      while (output.size > 1 && clockDirection(extract(point._1), extract(output.head), extract(output(1))) < 0) { output.pop() }
      output.push(point._1)
    })
    output
  }

  @inline final def convexPolygonContains[T](points: Seq[AbstractPoint], point: AbstractPoint): Boolean = {
    // A point is inside a convex polygon if it is on the same side of each segment
    if (points.length < 2) return false
    var consensusSide: Option[Boolean] = None
    var i = 0
    while(i < points.length) {
      val p1 = points(i)
      val p2 = points((i + 1) % points.length)
      val side = (point.y - p1.y) * (p2.x - p1.x) - (point.x - p1.x) * (p2.y - p1.y)
      if (side != 0) {
        val binarySide = side > 0
        consensusSide = consensusSide.orElse(Some(binarySide))
        if ( ! consensusSide.contains(binarySide)) return false
      }
      i += 1
    }
    true
  }

  @inline final def projectedPointOnLine(p: Pixel, v1: Pixel, v2: Pixel): Pixel = {
    val e1x       = v2.x - v1.x.toDouble
    val e1y       = v2.y - v1.y.toDouble
    val e2x       = p.x - v1.x.toDouble
    val e2y       = p.y - v1.y.toDouble
    val edot      = e1x * e2x + e1y * e2y
    val eLength2  = Math.max(1, e1x * e1x + e1y * e1y)
    Pixel(
      (v1.x + (e1x * edot) / eLength2).toInt,
      (v1.y + (e1y * edot) / eLength2).toInt)
  }

  @inline final def projectedPointOnSegment(p: Pixel, v1: Pixel, v2: Pixel): Pixel = {
    val on = projectedPointOnLine(p, v1, v2)
    val segmentLengthSquared = v1.pixelDistanceSquared(v2)
    val isOnSegment = on.pixelDistanceSquared(v1) < segmentLengthSquared && on.pixelDistanceSquared(v2) < segmentLengthSquared
    if (isOnSegment) on else Seq(v1, v2).minBy(_.pixelDistanceSquared(p))
  }

  /**
    * Given a looped sequence of items, returns the shortest subsequence starting with one element passing into another element, either forward or backward with respect to the sequence.
    */
  @inline final def shortestItinerary[T](from: T, to: T, rotation: Seq[T]): Seq[T] = {
    val indexFrom       = rotation.indexOf(from)
    val indexTo         = rotation.indexOf(to)
    val distance        = Math.abs(indexTo - indexFrom)
    val forwardRotation = (indexTo > indexFrom) == (distance * 2 <= rotation.length)
    val orderedRotation = if (forwardRotation) rotation.view else rotation.view.reverse
    if (indexFrom < indexTo) orderedRotation.slice(indexFrom, indexTo + 1) else orderedRotation.drop(indexFrom) ++ orderedRotation.take(indexTo + 1)
  }

  /**
    * Given a looped sequence of items, returns a subsequence starting with one element passing into another element in the same order as the sequence.
    */
  @inline final def itinerary[T](from: T, to: T, rotation: Seq[T]): Seq[T] = {
    val indexFrom = rotation.indexOf(from)
    val indexTo   = rotation.indexOf(to)
    if (indexFrom < indexTo) rotation.view.slice(indexFrom, indexTo + 1) else rotation.view.drop(indexFrom) ++ rotation.view.take(indexTo + 1)
  }
}
