package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object ScreenGroundskeeper {
  
  def render() {
    With.game.drawTextScreen(5, 25, "Updated:")
    DrawScreen.column(
      5, 50,
      With.groundskeeper.updated.map(_.toString))
    With.game.drawTextScreen(225, 25,"Unplaced:")
    DrawScreen.column(
      225, 50,
      With.groundskeeper.unplaced.map(_.toString))
    With.game.drawTextScreen(445, 25,"Placed:")
    DrawScreen.table(
      445, 50,
      With.groundskeeper.placed.map(pair => Vector(pair._2.toString, pair._1.toString)))
  }
}
