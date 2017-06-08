package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Pixels.Pixel
import ProxyBwapi.Players.Players

import scala.collection.JavaConverters._

object VisualizeBullets {
  
  def render() {
    With.game.getBullets.asScala
      .filter(_.isVisible)
      .filter(_.exists)
      .foreach(bullet => {
        DrawMap.circle(
          new Pixel(bullet.getPosition).add(-2, -2),
          2,
          Players.get(bullet.getPlayer).colorDark,
          true)
        DrawMap.circle(
          new Pixel(bullet.getPosition).add(2, -2),
          2,
          Players.get(bullet.getPlayer).colorDark,
          true)
        DrawMap.triangle(
          new Pixel(bullet.getPosition).add(-4, -1),
          new Pixel(bullet.getPosition).add(4, -1),
          new Pixel(bullet.getPosition).add(0, 3),
          Players.get(bullet.getPlayer).colorDark,
          true)
      })
  }
}
