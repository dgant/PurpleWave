package Debugging.Visualization

import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import scala.collection.JavaConverters._

object VisualizeBullets {
  
  def render() {
    With.game.getBullets.asScala
      .filter(_.isVisible)
      .filter(_.exists)
      .foreach(bullet => {
        With.game.drawBoxMap(
          bullet.getPosition.add(-2, -2),
          bullet.getPosition.add(2, 2),
          DrawMap.playerColor(bullet.getPlayer),
          true)
        With.game.drawLineMap(
          bullet.getPosition,
          bullet.getPosition.add( -bullet.getVelocityX.toInt, -bullet.getVelocityY.toInt),
          DrawMap.playerColor(bullet.getPlayer))
      })
  }
}
