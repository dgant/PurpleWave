package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowExplosions extends View {
  override def renderMap(): Unit = {
    With.coordinator.explosions.all.foreach(explosion =>
      if (With.units.ours.exists(explosion.affects)) {
        explosion.draw()
      })
  }
}
