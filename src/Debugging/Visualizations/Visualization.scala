package Debugging.Visualizations

import Debugging.Visualizations.Views.Combat.VisualizeBattles
import Debugging.Visualizations.Views.Economy.{VisualizeEconomy, VisualizeScheduler}
import Debugging.Visualizations.Views.Fun._
import Debugging.Visualizations.Views.Geography.{VisualizeBases, VisualizeChokepoints, VisualizeGeography, VisualizeRealEstate}
import Debugging.Visualizations.Views.Micro._
import Debugging.Visualizations.Views.Performance.VisualizePerformance
import Debugging.Visualizations.Views.Planning.{VisualizePlans, VisualizeResources}
import Debugging.Visualizations.Views._
import Lifecycle.With

object Visualization {
  
  val lineHeightSmall = 9
  
  def render() {
    if (With.configuration.visualize) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  
      if (With.configuration.visualizeHappyVision) {
        VisualizeBlackScreen.render()
        VisualizeGeography.render()
        VisualizeVectorUnits.render()
        VisualizeBullets.render()
        VisualizeBlackScreenOverlay.render("Retro Arcade Happy Vision")
      }
      else if (With.configuration.visualizeTextOnly) {
        VisualizeBlackScreen.render()
        VisualizeTextOnly.render()
        VisualizeGeography.render()
        VisualizeBlackScreenOverlay.render("Work-Friendly Retro Console Vision")
      }
  
      if (With.configuration.visualizeGeography)          VisualizeGeography            .render()
      if (With.configuration.visualizeChokepoints)        VisualizeChokepoints          .render()
      if (With.configuration.visualizeRealEstate)         VisualizeRealEstate           .render()
      if (With.configuration.visualizeGrids)              VisualizeGrids                .render()
      if (With.configuration.visualizeBases)              VisualizeBases                .render()
      if (With.configuration.visualizeUnitsForeign)       VisualizeUnitsForeign         .render()
      if (With.configuration.visualizeUnitsOurs)          VisualizeUnitsOurs            .render()
      if (With.configuration.visualizeHitPoints)          VisualizeHitPoints            .render()
      if (With.configuration.visualizeHeuristicMovement)  VisualizeMovementHeuristics   .render()
      if (With.configuration.visualizeBullets)            VisualizeBullets              .render()
      if (With.configuration.visualizePlans)              VisualizePlans                .render()
      if (With.configuration.visualizeBattles)            VisualizeBattles              .render()
      if (With.configuration.visualizeEconomy)            VisualizeEconomy              .render()
      if (With.configuration.visualizeResources)          VisualizeResources            .render()
      if (With.configuration.visualizeScheduler)          VisualizeScheduler            .render()
      if (With.configuration.visualizePerformance)        VisualizePerformance          .render()
      
    }
  }
}
