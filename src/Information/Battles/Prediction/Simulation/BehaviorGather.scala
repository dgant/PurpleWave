package Information.Battles.Prediction.Simulation

import Mathematics.Maff
import Utilities.Time.Forever

object BehaviorGather extends SimulacrumBehavior {
  val fighting: Boolean = false
  override def act(simulacrum: Simulacrum): Unit = {
    val bases = simulacrum.player.bases
    if (bases.nonEmpty) {
      val from = simulacrum.pixel.walkablePixel
      var base = bases.head
      var best = from.groundPixels(base.heart)
      var i = 1
      while (i < bases.length) {
        val nextBase = bases(i)
        val next = from.groundPixels(nextBase.heart)
        if (next < best) {
          base = nextBase
          best = next
        }
        i += 1
      }
      val to = base.heart.center
      if (simulacrum.pixelDistanceCenter(to) > 16) {
        simulacrum.move(to, Some("Gather"))
      } else {
        simulacrum.sleep(Forever(), Some("Gathering"))
      }
    }
  }
}
