package Mathematics.Functions

import Lifecycle.With

import scala.util.Random

trait Sample {

  @inline final def softmax[T](values: Seq[T], extract: (T) => Double): Seq[(T, Double)] = {
    val sum = values.map(value => Math.pow(Math.E, extract(value))).sum
    values.map(value => (value, Math.pow(Math.E, extract(value)) / sum))
  }

  @inline final def sample[T](seq: Seq[T]): T = {
    seq(Random.nextInt(seq.size))
  }
  @inline final def sampleSet[T](set: Set[T]): T = {
    set.iterator.drop(Random.nextInt(set.size)).next
  }

  @inline final def sampleWeighted[T](seq: Seq[T], extract: (T) => Double): Option[T] = {
    if (seq.isEmpty) return None
    val denominator = seq.map(extract).map(v => Math.max(v, 0)).sum
    val numerator   = Random.nextDouble() * denominator
    var passed      = 0.0
    var index       = 0
    if (denominator <= 0) return Some(sample(seq))
    for (value <- seq) {
      passed += Math.max(0, extract(value))
      if (passed > numerator) {
        return Some(value)
      }
    }
    // Oops, we screwed up.
    With.logger.warn(f"Failed to get weighted sample! Numerator: $numerator. Denominator: $denominator")
    Some(sample(seq))
  }

  @inline final def softmaxSample[T](seq: Seq[T], extract: (T) => Double): Option[T] = {
    val softmaxed: Seq[(T, Double)] = softmax(seq, extract)
    sampleWeighted[(T, Double)](softmaxed, v => v._2).map(_._1)
  }
}
