package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object ScreenGroundskeeper {
  
  def render() {
    
    With.game.drawTextScreen(5, 25,"Unplaced:")
    DrawScreen.column(
      5, 50,
      With.groundskeeper.sortByPriority(
        With.groundskeeper.proposals.diff(With.groundskeeper.proposalPlacements.keySet))
        .map(_.toString))
    
    With.game.drawTextScreen(325, 25,"Placed:")
    DrawScreen.table(
      325, 50,
      With.groundskeeper.sortByPriority(
        With.groundskeeper.proposalPlacements.keys)
        .map(key => Vector(With.groundskeeper.proposalPlacements(key).tile.toString, key.toString)))
  }
}
