package Information.Battles.Prediction.Simulation

import Utilities.Time.Forever

object BehaviorGather extends SimulacrumBehavior {
  val fighting: Boolean = false
  override def act(simulacrum: Simulacrum): Unit = {
    val intentGather = if (simulacrum.isFriendlyCached) simulacrum.realUnit.friendly.flatMap(_.intent.toGather) else None
    if (intentGather.isDefined) {
      gather(simulacrum, intentGather.get.pixel)
    } else {
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
        gather(simulacrum, base.heart.center)
      }
    }
  }

  @inline private def gather(simulacrum: Simulacrum, to: Mathematics.Points.Pixel): Unit = {
    if (simulacrum.pixelDistanceCenter(to) > 16) {
      simulacrum.move(to, Some("Gather"))
    } else {
      simulacrum.sleep(Forever(), Some("Gathering"))
    }
  }
}
