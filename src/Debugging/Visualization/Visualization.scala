package Debugging.Visualization

import Debugging.Visualization.Views._
import Startup.With

object Visualization {
  def onFrame() {
    if (With.configuration.enableVisualization) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  
      if (With.configuration.enableVisualizationVectorUnits) {
        VisualizeBlackScreen.render("Retro Arcade Happy Vision")
        VisualizeVectorUnits.render()
        VisualizeGeography.render()
        VisualizeBullets.render()
      }
      else if (With.configuration.enableVisualizationTextOnly) {
        VisualizeBlackScreen.render("Work-Friendly Console Vision")
        VisualizeTextOnly.render()
        VisualizeGeography.render()
      }
      
      if (With.configuration.enableVisualizationBattles)            VisualizeBattles              .render()
      if (With.configuration.enableVisualizationBases)              VisualizeBases                .render()
      if (With.configuration.enableVisualizationBullets)            VisualizeBullets              .render()
      if (With.configuration.enableVisualizationChokepoints)        VisualizeChokepoints          .render()
      if (With.configuration.enableVisualizationEconomy)            VisualizeEconomy              .render()
      if (With.configuration.enableVisualizationGeography)          VisualizeGeography            .render()
      if (With.configuration.enableVisualizationGrids)              VisualizeGrids                .render()
      if (With.configuration.enableVisualizationPerformance)        VisualizePerformance          .render()
      if (With.configuration.enableVisualizationPlans)              VisualizePlans                .render()
      if (With.configuration.enableVisualizationResources)          VisualizeResources            .render()
      if (With.configuration.enableVisualizationScheduler)          VisualizeScheduler            .render()
      if (With.configuration.enableVisualizationMovementHeuristics) VisualizeMovementHeuristics   .render()
      if (With.configuration.enableVisualizationUnitsForeign)       VisualizeUnitsForeign         .render()
      if (With.configuration.enableVisualizationUnitsOurs)          VisualizeUnitsOurs            .render()
      if (With.configuration.enableVisualizationZones)              VisualizeZones                .render()
    }
  }
}
