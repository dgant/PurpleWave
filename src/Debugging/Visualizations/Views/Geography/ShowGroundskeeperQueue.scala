package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowGroundskeeperQueue extends View {
  
  override def renderMap() {
    
  }
  
  override def renderScreen() {
    
    With.game.drawTextScreen(5, 25, "Unplaced:")
    DrawScreen.column(
      5, 50,
      With.groundskeeper.proposals.diff(
        With.groundskeeper.proposalPlacements
          .filter(_._2.tile.isDefined)
          .keySet)
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
      matches.map(_.requirement.toString.take(5)))
    DrawScreen.column(
      265, 50,
      matches.map(" => " + _.requirement.toString.take(5)))
    
    With.game.drawTextScreen(365, 25, "Placed:")
    DrawScreen.table(
      365, 50,
      With.groundskeeper.proposalPlacements
        .filter(_._2.tile.isDefined)
        .keys
        .toVector
        .sortBy(_.proposer.priority)
        .map(key => Vector(With.groundskeeper.proposalPlacements(key).tile.toString, key.toString)))
  }
}
