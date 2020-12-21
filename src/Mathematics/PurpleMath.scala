package Mathematics

import Lifecycle.With
import Mathematics.Points._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object PurpleMath {

  val halfPI: Double = Math.PI / 2
  val twoPI: Double = 2 * Math.PI
  val sqrt2: Double = Math.sqrt(2)
  val sqrt2f: Float = sqrt2.toFloat

  @inline final def mean(values: Traversable[Double]): Double = {
    if (values.isEmpty)
      0.0
    else
      values.sum / values.size
  }
  
  @inline final def centroid(values: Iterable[Pixel]): Pixel = {
    if (values.isEmpty) return SpecificPoints.middle
    // Faster than .sum
    var x: Int = 0
    var y: Int = 0
    values.foreach(v => {
      x += v.x
      y += v.y
    })
    Pixel(x / values.size, y / values.size)
  }

  @inline final def centroidTiles(values: Iterable[Tile]): Tile = {
    if (values.isEmpty) return SpecificPoints.tileMiddle
    // Faster than .sum
    var x: Int = 0
    var y: Int = 0
    values.foreach(v => {
      x += v.x
      y += v.y
    })
    Tile(x / values.size, y / values.size)
  }

  @inline final def nanToN(value: Double, n: Double): Double = {
    if (value.isNaN || value.isInfinity) n else value
  }

  @inline final def nanToZero(value: Double): Double = nanToN(value, 0)
  @inline final def nanToOne(value: Double): Double = nanToN(value, 1)
  @inline final def nanToInfinity(value: Double): Double = nanToN(value, Double.PositiveInfinity)
  
  @inline final def clampRatio(value: Double, ratio: Double): Double = clamp(value, ratio, 1.0 / ratio)
  
  @inline final def clamp(value: Int, bound0: Int, bound1: Int): Int = {
    val min = Math.min(bound0, bound1)
    val max = Math.max(bound0, bound1)
    Math.min(max, Math.max(min, value))
  }
  
  @inline final def clamp(value: Double, value1: Double, value2: Double): Double = {
    val min = Math.min(value1, value2)
    val max = Math.max(value1, value2)
    Math.min(max, Math.max(value, min))
  }
  
  @inline final def clampToOne(value: Double): Double = clamp(value, 0.0, 1.0)
  
  @inline final def signum(int: Int)        : Int = if (int == 0) 0 else if (int < 0) -1 else 1
  @inline final def signum(double: Double)  : Int = if (double == 0.0) 0 else if (double < 0) -1 else 1
  @inline final def forcedSignum(int: Int)  : Int = if (int < 0) -1 else 1
  
  val twoPi: Double = Math.PI * 2.0
  @inline final def normalize0To2Pi(angleRadians: Double): Double = {
    if      (angleRadians < 0) normalize0To2Pi(angleRadians + twoPi)
    else if (angleRadians > twoPi) normalize0To2Pi(angleRadians - twoPi)
    else    angleRadians
  }
  @inline final def normalizeAroundZero(angleRadians: Double): Double = {
    if      (angleRadians < -Math.PI) normalize0To2Pi(angleRadians + twoPi)
    else if (angleRadians > Math.PI) normalize0To2Pi(angleRadians - twoPi)
    else    angleRadians
  }
  @inline final def radiansTo(from: Double, to: Double): Double = {
    val distance = normalize0To2Pi(to - from)
    if (distance > Math.PI) distance - twoPI else distance
  }

  @inline final def geometricMean(values: Iterable[Double]): Double = {
    if (values.isEmpty) return 1.0
    Math.pow(values.product, 1.0 / values.size)
  }

  @inline final def fromBoolean(value: Boolean): Int = if (value) 1 else 0
  @inline final def toBoolean(value: Int): Boolean = value != 0

  @inline final def broodWarDistance(a: AbstractPoint, b: AbstractPoint): Double = broodWarDistance(a.x, a.y, b.x, b.y)
  @inline final def broodWarDistance(x0: Int, y0: Int, x1: Int, y1: Int): Double = {
    val dx = Math.abs(x0 - x1)
    val dy = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d < D / 4) {
      return D
    }
    D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256
  }
  @inline final def broodWarDistanceDouble(x0: Double, y0: Double, x1: Double, y1: Double): Double = {
    val dx  = Math.abs(x0 - x1)
    val dy  = Math.abs(y0 - y1)
    val d   = Math.min(dx, dy)
    val D   = Math.max(dx, dy)
    if (d < D / 4) {
      return D
    }
    D - D / 16 + d * 3 / 8 - D / 64 + d * 3 / 256
  }
  @inline final def broodWarDistanceBox(
    p00: AbstractPoint,
    p01: AbstractPoint,
    p10: AbstractPoint,
    p11: AbstractPoint)
    : Double = broodWarDistanceBox(
      p00.x, p00.y,
      p01.x, p01.y,
      p10.x, p10.y,
      p11.x, p11.y)
  @inline final def broodWarDistanceBox(
    x00: Int, y00: Int,
    x01: Int, y01: Int,
    x10: Int, y10: Int,
    x11: Int, y11: Int)
    : Double = {
    if (x11 < x00) {
      if (y11 < y00) {
        return PurpleMath.broodWarDistance(x11, y11, x00, y00)
      } else if (y10 > y01) {
        return PurpleMath.broodWarDistance(x11, y10, x00, y01)
      } else {
        return x00 - x11
      }
    } else if (x10 > x01) {
      if (y11 < y00) {
        return PurpleMath.broodWarDistance(x10, y11, x01, y00)
      } else if (y10 > y01) {
        return PurpleMath.broodWarDistance(x10, y10, x01, y01)
      } else {
        return x10 - x01
      }
    } else if (y11 < y00) {
      return y00 - y11
    } else if (y10 > y01) {
      return y10 - y01
    }
    0
  }

  @inline final def distanceFromLineSegment(
    point: AbstractPoint,
    segmentStart: AbstractPoint,
    segmentEnd: AbstractPoint)
      : Double = {
    val x = point.x
    val y = point.y
    val x0 = segmentStart.x
    val y0 = segmentStart.y
    val x1 = segmentEnd.x
    val y1 = segmentEnd.y

    val dx0 = x - x0
    val dy0 = y - y0
    val dx1 = x1 - x0
    val dy1 = y1 - y0

    val dotProduct = dx0 * dx1 + dy0 * dy1
    val lengthSquared = dx1 * dx1 + dy1 * dy1
    var param = -1.0
    if (lengthSquared != 0) {
      param = dotProduct / lengthSquared
    }

    var xx = 0.0
    var yy = 0.0

    if (param < 0) {
      xx = x0
      yy = y0
    }
    else if (param > 1) {
      xx = x1
      yy = y1
    }
    else {
      xx = x0 + param * dx1
      yy = y0 + param * dy1
    }

    val dx = x - xx
    val dy = y - yy

    Math.sqrt(dx * dx + dy * dy)
  }

  @inline final def fastSigmoid(x: Float): Float = 0.5f + fastTanh(x) / 2.0f
  @inline final def fastSigmoid(x: Double): Double = 0.5 + fastTanh(x) / 2.0
  @inline final def fastTanh(x: Float): Float = {
    if (x.isPosInfinity) return 1f
    if (x.isNegInfinity) return -1f
    x / (1f + Math.abs(x))
  }
  @inline final def fastTanh(x: Double): Double = {
    if (x.isPosInfinity) return 1.0
    if (x.isNegInfinity) return -1.0
    x / (1.0 + Math.abs(x))
  }

  // Via https://www.dsprelated.com/showarticle/1052.php
  @inline final def fastAtan(r: Double): Double = (0.97239411 - 0.19194795 * r * r) * r

  // Via https://www.dsprelated.com/showarticle/1052.php
  @inline final def fastAtan2(y: Double, x: Double): Double = {
    if (x == 0) {
      if (y > 0) halfPI else - halfPI
    } else if (x * x > y * y) {
      val a = fastAtan(y / x)
      if (x > 0) a else if (y > 0) a + Math.PI else a - Math.PI
    } else {
      val a = fastAtan(x / y)
      if (y > 0) halfPI - a else - halfPI - a
    }
  }

  @inline final def slowAtan2(y: Double, x: Double): Double = {
   normalize0To2Pi(Math.atan2(y, x))
  }

  @inline final def weightedMean(values: Seq[(Double, Double)]): Double = {
    var numerator = 0.0
    var denominator = 0.0
    for (value <- values) {
      numerator += value._1 * value._2
      denominator += value._2
    }
    numerator / denominator
  }

  @inline final def softmax[T](values: Seq[T], extract: (T) => Double): Seq[(T, Double)] = {
    val sum = values.map(value => Math.pow(Math.E, extract(value))).sum
    values.map(value => (value, Math.pow(Math.E, extract(value)) / sum))
  }

  @inline final def sample[T](seq: Seq[T]): T = {
    seq(Random.nextInt(seq.size))
  }

  @inline final def sampleWeighted[T](seq: Seq[T], extract: (T) => Double): Option[T] = {
    if (seq.isEmpty) return None
    val denominator = seq.map(extract).sum
    val numerator   = Random.nextDouble() * denominator
    val shuffled    = Random.shuffle(seq).toVector
    var passed      = 0.0
    var index       = 0
    for (value <- seq) {
      passed += extract(value)
      if (passed > numerator) {
        return Some(value)
      }
    }
    // Oops, we screwed up.
    With.logger.warn("Failed to get weighted sample!")
    Some(seq.maxBy(extract))
  }

  @inline final def softmaxSample[T](seq: Seq[T], extract: (T) => Double): Option[T] = {
    val softmaxed: Seq[(T, Double)] = softmax(seq, extract)
    sampleWeighted[(T, Double)](softmaxed, v => v._2).map(_._1)
  }

  // Gaussian expansion of N + (N-1) + (N-2) + ... + 1
  @inline final def gaussianExpansion(k: Int): Int = {
    if (k % 2 == 0)
      (k + 1) * (k / 2)
    else
      (k + 2) * (k / 2) + 1
  }

  @inline final def square(k: Int): Int = k * k
  @inline final def square(k: Float): Float = k * k
  @inline final def square(k: Double): Double = k * k

  /**
    * @return 0 if colinear, 1 if clockwise, -1 if counterclockwise
    */
  @inline final def clockDirection(a: AbstractPoint, b: AbstractPoint, c: AbstractPoint): Int = {
    // See https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order/1180256#1180256
    // and https://www.geeksforgeeks.org/orientation-3-ordered-points/
    signum((b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y))
  }

  @inline final def convexHull(points: Seq[Point]): Seq[Point] = {
    // See https://en.wikipedia.org/wiki/Graham_scan
    if (points.isEmpty) return Seq.empty
    if (points.size == 2) return Seq(points.head, points.last)

    // Find an extremum, guaranteed to be on the hull
    val yMax = points.view.map(_.y).max
    val origin = points.view.filter(_.y == yMax).maxBy(_.x)

    // Get points sorted by polar angle with the origin, discarding closer points
    var sortedPoints = new ArrayBuffer[(Point, Double)]
    sortedPoints ++= points.view.map(p => (p, origin.radiansTo(p)))
    sortedPoints = sortedPoints.sortBy(_._2).sortBy(_._1 != origin)
    var i: Int = 1
    while(i < sortedPoints.length) {
      val a = sortedPoints(i - 1)
      val b = sortedPoints(i)
      if (a._2 == b._2) {
        sortedPoints.remove(if (origin.distanceSquared(a._1) < origin.distanceSquared(b._1)) i - 1 else i)
      } else {
        i += 1
      }
    }

    val stack = new mutable.Stack[Point]
    sortedPoints.foreach(point => {
      while (stack.size > 1 && clockDirection(point._1, stack.head, stack(1)) < 0)
        stack.pop()
      stack.push(point._1)
    })
    stack
  }

  @inline final def projectedPointOnLine(p: Pixel, v1: Pixel, v2: Pixel): Pixel = {
    val e1x = v2.x - v1.x.toDouble
    val e1y = v2.y - v1.y.toDouble
    val e2x = p.x - v1.x.toDouble
    val e2y = p.y - v1.y.toDouble
    val edot = e1x * e2x + e1y * e2y
    val eLength2 = Math.max(1, e1x * e1x + e1y * e1y)
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

  @inline final def toInt(value: Boolean): Int = if (value) 1 else 0

  @inline final def toSign(value: Boolean): Int = if (value) 1 else -1
}
