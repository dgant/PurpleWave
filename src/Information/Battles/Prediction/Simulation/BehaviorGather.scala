package Information.Battles.Prediction.Simulation

import Mathematics.Maff
import Utilities.Time.Forever

object BehaviorGather extends SimulacrumBehavior {
  val fighting: Boolean = false
  override def act(simulacrum: Simulacrum): Unit = {
    val base = Maff.minBy(simulacrum.player.bases)(b => simulacrum.pixel.walkablePixel.groundPixels(b.heart))
    base.foreach(b => {
      val to = b.heart.center
      if (simulacrum.pixelDistanceCenter(to) > 16) {
        simulacrum.move(to, Some("Gather"))
      } else {
        simulacrum.sleep(Forever(), Some("Gathering"))
      }
    })
  }
}
