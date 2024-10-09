package Mathematics.Functions

import Mathematics.Maff
import Mathematics.Points.{Pixel, Points, Tile}
import Utilities.?

import scala.collection.mutable

trait Reduce {

  @inline final def clamp(value: Int, bound1: Int, bound2: Int): Int = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    Math.min(max, Math.max(min, value))
  }
  @inline final def clamp(value: Double, bound1: Double, bound2: Double): Double = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    Math.min(max, Math.max(value, min))
  }
  @inline final def clamp01(value: Int)   : Int     = clamp(value, 0, 1)
  @inline final def clamp01(value: Double): Double  = clamp(value, 0, 1)

  @inline final def betweenI(value: Int, bound1: Int, bound2: Int): Boolean = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    value >= min && value <= max
  }
  @inline final def betweenE(value: Int, bound1: Int, bound2: Int): Boolean = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    value > min && value < max
  }
  @inline final def betweenI(value: Double, bound1: Double, bound2: Double): Boolean = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    value >= min && value <= max
  }
  @inline final def betweenE(value: Double, bound1: Double, bound2: Double): Boolean = {
    val min = Math.min(bound1, bound2)
    val max = Math.max(bound1, bound2)
    value > min && value < max
  }

  @inline final def orElse[T](x: Iterable[T]*): Iterable[T] = x.dropRight(1).find(_.nonEmpty).getOrElse(x.last)
  @inline final def orElseFiltered[T](x: Iterable[T]*)(predicate: T => Boolean): Iterable[T] = x
    .find(_.exists(predicate))
    .map(_.filter(predicate)).getOrElse(Iterable.empty)

  @inline final def ??[T >: Null](x: T*): T = x.find(_ != null).orNull

  @inline final def mode[T](values: Traversable[T]): T = values.groupBy(x => x).maxBy(_._2.size)._1
  @inline final def modeOpt[T](values: Traversable[T]): Option[T] = ?(values.isEmpty, None, Some(Maff.mode(values)))

  @inline final def median[T: Ordering](values: Traversable[T]): T = {
    val pq = new mutable.PriorityQueue[T]
    pq ++= values
    val halfLength = Maff.div2(pq.length)
    var i = 0
    while (i < halfLength) {
      pq.dequeue()
      i += 1
    }
    pq.dequeue()
  }
  @inline final def medianOpt[T: Ordering](values: Traversable[T]): Option[T] = {
    ?(values.isEmpty, None,  Some(median(values)))
  }

  @inline final def mean(values: TraversableOnce[Double]): Double = {
    if (values.isEmpty) 0.0 else {
      var numerator, denominator = 0.0
      values.foreach(value => { numerator += value;  denominator += 1.0 })
      numerator / denominator
    }
  }
  @inline final def meanL(values: TraversableOnce[Long]): Double = {
    if (values.isEmpty) 0L else {
      var numerator, denominator = 0L
      values.foreach(value => { numerator += value;  denominator += 1L })
      numerator / denominator
    }
  }
  @inline final def meanOpt(values: TraversableOnce[Double]): Option[Double] = {
    if (values.isEmpty) None else Some(mean(values))
  }

  @inline final def geometricMean(values: Iterable[Double]): Double = {
    if (values.isEmpty) 1.0 else Math.pow(values.product, 1.0 / values.size)
  }

  @inline final def vmean(values: Double*): Double = {
    mean(values)
  }

  @inline final def vmin[A](values: A*)(implicit cmp: scala.Ordering[A]): A = {
    values.min(cmp)
  }

  @inline final def vmax[A](values: A*)(implicit cmp: scala.Ordering[A]): A = {
    values.max(cmp)
  }

  @inline final def min[A](values: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (values.isEmpty) None else Some(values.min(cmp))
  }

  @inline final def max[A](values: TraversableOnce[A])(implicit cmp: scala.Ordering[A]): Option[A] = {
    if (values.isEmpty) None else Some(values.max(cmp))
  }

  @inline final def rms(values: TraversableOnce[Double]): Option[Double] = {
    if (values.isEmpty) None else {
      var numerator = 0.0
      var denominator = 0.0
      values.foreach(value => { numerator += value * value; denominator += 1.0 })
      val sumOfSquares  = numerator / denominator
      val output        = Math.sqrt(sumOfSquares)
      Some(sumOfSquares)
    }
  }

  @inline final def minBy[A, B](values: TraversableOnce[A])(feature: A => B)(implicit ordering: Ordering[B]): Option[A] = {
    // The Scala-y implementation would be:
    //   values.reduceOption(Ordering.by(feature).min)
    // but this is a performance critical function so we're unrolling it.
    var first           = true
    var output      : A = null.asInstanceOf[A]
    var outputValue : B = null.asInstanceOf[B]
    values.foreach(next => {
      val nextValue = feature(next)
      if (first) {
        first = false
        output      = next
        outputValue = feature(next)
      } else if (ordering.compare(nextValue, outputValue) < 0) {
        output      = next
        outputValue = nextValue
      }
    })
    if (first) None else Some(output)
  }

  @inline final def maxBy[A, B](values: TraversableOnce[A])(feature: A => B)(implicit ordering: Ordering[B]): Option[A] = {
    // The Scala-y implementation would be:
    //   values.reduceOption(Ordering.by(feature).min)
    // but this is a performance critical function so we're unrolling it.
    var first           = true
    var output      : A = null.asInstanceOf[A]
    var outputValue : B = null.asInstanceOf[B]
    values.foreach(next => {
      val nextValue = feature(next)
      if (first) {
        first = false
        output      = next
        outputValue = feature(next)
      } else if (ordering.compare(nextValue, outputValue) > 0) {
        output      = next
        outputValue = nextValue
      }
    })
    if (first) None else Some(output)
  }

  @inline final def takeN[T](number: Int, iterable: Iterable[T])(implicit ordering: Ordering[T]): IndexedSeq[T] = {
    val queue = collection.mutable.PriorityQueue[T](iterable.toSeq: _*)
    (0 until Math.min(number, queue.size)).map(i => queue.dequeue())
  }

  @inline final def takePercentile[T](percentile: Double, iterable: Iterable[T])(implicit ordering: Ordering[T]): IndexedSeq[T] = {
    val nth = (iterable.size * Maff.clamp01(percentile)).toInt
    val queue = collection.mutable.PriorityQueue[T](iterable.toSeq: _*)
    (0 until nth).foreach(i => queue.dequeue())
    val finalSize = queue.size
    (0 until finalSize).map(i => queue.dequeue())
  }

  @inline final def centroid(values: TraversableOnce[Pixel]): Pixel = {
    if (values.isEmpty) return Points.middle
    var x, y, size: Int = 0
    values.foreach(v => { x += v.x; y += v.y; size += 1 })
    Pixel(x / size, y / size)
  }

  @inline final def centroidTiles(values: TraversableOnce[Tile]): Tile = {
    if (values.isEmpty) return Points.tileMiddle
    var x, y, size: Int = 0
    values.foreach(v => { x += v.x; y += v.y; size += 1 })
    Tile(x / size, y / size)
  }

  @inline final def exemplar(values: Traversable[Pixel]): Pixel = {
    if (values.isEmpty) return Points.middle
    val valuesCentroid = centroid(values)
    values.minBy(_.pixelDistanceSquared(valuesCentroid))
  }

  @inline final def exemplarTiles(values: Traversable[Tile]): Tile = {
    if (values.isEmpty) return Points.tileMiddle
    val valuesCentroid = centroidTiles(values)
    values.minBy(_.pixelDistance(valuesCentroid))
  }

  @inline final def exemplarOpt(values: Traversable[Pixel]): Option[Pixel] = {
    if (values.isEmpty) None else Some(exemplar(values))
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

  @inline final def weightedCentroid(values: Traversable[(Pixel, Double)]): Pixel = {
    if (values.isEmpty) return Points.middle
    var x, y, d: Double = 0
    values.foreach(v => { x += v._1.x * v._2;  y += v._1.y * v._2;  d += v._2 })
    Pixel((x / d).toInt, (y / d).toInt)
  }

  @inline final def weightedExemplar(values: Iterable[(Pixel, Double)]): Pixel = {
    val centroid = weightedCentroid(values)
    minBy(values)(_._1.pixelDistanceSquared(centroid)).map(_._1).getOrElse(Points.middle)
  }

  @inline final def weightedExemplarPercentile(values: Iterable[(Pixel, Double)], percentile: Double): Pixel = {
    val centroidAll = weightedCentroid(values)
    val closestValues = takePercentile(percentile, values)(Ordering.by(_._1.pixelDistanceSquared(centroidAll)))
    minBy(closestValues)(_._1.pixelDistanceSquared(centroidAll)).map(_._1).getOrElse(Points.middle)
  }
}
