package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object ScreenGroundskeeper {
  
  def render() {
    
    With.game.drawTextScreen(5, 25, "Unplaced:")
    DrawScreen.column(
      5, 50,
      With.groundskeeper.proposals.diff(With.groundskeeper.proposalPlacements.keySet)
        .toVector
        .sortBy(_.proposer.priority)
        .map(_.toString))
  
    With.game.drawTextScreen(165, 25, "Matched:")
    val matches =
      With.groundskeeper.requirementMatches
        .filter(r => r.proposal != r.requirement)
        .toList
        .sortBy(_.proposal.proposer.priority)
    DrawScreen.column(
      165, 50,
      matches.map(_.requirement.toString))
    DrawScreen.column(
      325, 50,
      matches.map(" => " + _.requirement.toString))
    
    With.game.drawTextScreen(485, 25, "Placed:")
    DrawScreen.table(
      485, 50,
      With.groundskeeper.proposalPlacements.keys
        .toVector
        .sortBy(_.proposer.priority)
        .map(key => Vector(With.groundskeeper.proposalPlacements(key).tile.toString, key.toString)))
  }
}
