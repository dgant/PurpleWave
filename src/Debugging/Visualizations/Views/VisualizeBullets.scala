package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import ProxyBwapi.Players.Players
import Utilities.EnrichPosition._

import scala.collection.JavaConverters._

object VisualizeBullets {
  
  def render() {
    With.game.getBullets.asScala
      .filter(_.isVisible)
      .filter(_.exists)
      .foreach(bullet => {
        DrawMap.circle(
          bullet.getPosition.add(-2, -2),
          2,
          Players.get(bullet.getPlayer).colorDark,
          true)
        DrawMap.circle(
          bullet.getPosition.add(2, -2),
          2,
          Players.get(bullet.getPlayer).colorDark,
          true)
        DrawMap.triangle(
          bullet.getPosition.add(-4, -1),
          bullet.getPosition.add(4, -1),
          bullet.getPosition.add(0, 3),
          Players.get(bullet.getPlayer).colorDark,
          true)
      })
  }
}
