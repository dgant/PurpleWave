package Strategery

import Lifecycle.With

import scala.collection.mutable
import scala.util.Random

trait Rolling {
  val rolls: mutable.HashMap[String, Boolean] = new mutable.HashMap
  def roll(key: String, probability: Double): Boolean = {
    if ( ! rolls.contains(key)) {
      val rolled = Random.nextDouble()
      val success = rolled <= probability
      With.logger.debug(f"Roll for $key ${if (success) "PASSED" else "FAILED"} (Rolled $rolled into probability $probability)")
      rolls(key) = success
    }
    rolls(key)
  }
}
