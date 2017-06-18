package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Players.Players

import scala.collection.JavaConverters._

object MapBullets {
  
  def render() {
    With.game.getBullets.asScala
      .filter(bullet => bullet.isVisible && bullet.exists && bullet.getPlayer != null)
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
