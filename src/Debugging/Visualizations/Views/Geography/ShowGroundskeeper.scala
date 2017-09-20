package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowGroundskeeper extends View {
  
  override def renderMap() {
    With.groundskeeper.proposalPlacements
      .filter(_._2.tile.nonEmpty)
      .foreach(placement => {
        DrawMap.tileRectangle(
          TileRectangle(
            placement._2.tile.get,
            placement._2.tile.get.add(placement._1.widthTiles.get, placement._1.heightTiles.get)
          ),
          color = Colors.DarkTeal
        )
        DrawMap.label(placement._1.toString, placement._2.tile.get.add(placement._1.widthTiles.get / 2, 0).pixelCenter)
    })
  }
  
  override def renderScreen() {
    
    With.game.drawTextScreen(5, 5 * With.visualization.lineHeightSmall, "Unplaced:")
    DrawScreen.column(
      5, 7 * With.visualization.lineHeightSmall,
      With.groundskeeper.proposals.diff(
        With.groundskeeper.proposalPlacements
          .filter(_._2.tile.isDefined)
          .keySet)
        .toVector
        .sortBy(_.proposer.priority)
        .map(_.toString.take(30)))
  
    With.game.drawTextScreen(165, 5 * With.visualization.lineHeightSmall, "Matched:")
    val matches =
      With.groundskeeper.requirementMatches
        .filter(r => r.proposal != r.requirement)
        .toList
        .sortBy(_.proposal.proposer.priority)
    DrawScreen.column(
      165, 7 * With.visualization.lineHeightSmall,
      matches.map(_.requirement.toString.take(15)))
    DrawScreen.column(
      265, 7 * With.visualization.lineHeightSmall,
      matches.map(" => " + _.proposal.toString.take(15)))
    
    With.game.drawTextScreen(365, 5 * With.visualization.lineHeightSmall, "Placed:")
    DrawScreen.table(
      365, 7 * With.visualization.lineHeightSmall,
      With.groundskeeper.proposalPlacements
        .filter(_._2.tile.isDefined)
        .keys
        .toVector
        .sortBy(_.proposer.priority)
        .map(key => Vector(With.groundskeeper.proposalPlacements(key).tile.toString, key.toString)))
  }
}
