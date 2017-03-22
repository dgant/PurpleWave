package Debugging.Visualization

import Startup.With
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
          DrawMap.playerColor(bullet.getPlayer),
          true)
        DrawMap.circle(
          bullet.getPosition.add(2, -2),
          2,
          DrawMap.playerColor(bullet.getPlayer),
          true)
        DrawMap.triangle(
          bullet.getPosition.add(-4, -1),
          bullet.getPosition.add(4, -1),
          bullet.getPosition.add(0, 3),
          DrawMap.playerColor(bullet.getPlayer),
          true)
      })
  }
}
