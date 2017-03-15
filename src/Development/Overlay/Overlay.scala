package Development.Overlay

import Startup.With

object Overlay {
  def onFrame() {
    if (With.configuration.enableOverlay) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
      if (With.configuration.enableOverlayBattles)        DrawBattles.draw
      if (With.configuration.enableOverlayEconomy)        DrawEconomy.draw
      if (With.configuration.enableOverlayGrids)          DrawGrids.draw
      if (With.configuration.enableOverlayUnits)          DrawUnitsOurs.draw
      if (With.configuration.enableOverlayPerformance)    DrawPerformance.draw
      if (With.configuration.enableOverlayPlans)          DrawPlans.draw
      if (With.configuration.enableOverlayResources)      DrawResources.draw
      if (With.configuration.enableOverlayScheduler)      DrawScheduler.draw
      if (With.configuration.enableOverlayTerrain)        DrawTerrain.draw
      if (With.configuration.enableOverlayTrackedUnits)   DrawTrackedUnits.draw
    }
  }
}
