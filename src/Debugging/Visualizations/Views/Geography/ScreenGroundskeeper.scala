package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object ScreenGroundskeeper {
  
  def render() {
    With.game.drawTextScreen(5, 25, "Updated:")
    DrawScreen.column(
      5, 50,
      With.groundskeeper.sortByPriority(
        With.groundskeeper.updated)
        .map(_.toString))
    
    With.game.drawTextScreen(225, 25,"Unplaced:")
    DrawScreen.column(
      225, 50,
      With.groundskeeper.sortByPriority(
        With.groundskeeper.unplaced)
        .map(_.toString))
    
    With.game.drawTextScreen(445, 25,"Placed:")
    DrawScreen.table(
      445, 50,
      With.groundskeeper.sortByPriority(
        With.groundskeeper.placed.keys)
        .map(key => Vector(With.groundskeeper.placed(key).toString, key.toString)))
  }
}
