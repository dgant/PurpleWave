package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowTextOnly extends DebugView {
  
  def render(): Unit = {
    ShowBlackScreen.renderScreen()
    renderTerrain()
    renderUnits()
    ShowPlayerNames.renderScreen("Work-Friendly Retro Console Vision")
  }

  def renderTerrain(): Unit = {
    TileRectangle(
      With.viewport.start.tile,
      With.viewport.end.tile)
      .expand(2, 2)
      .tiles
      .filterNot(_.walkable)
      .map(_.center)
      .foreach(DrawMap.label("X", _))
  }

  def renderUnits(): Unit = {
    With.units.all.filter(_.visible).filter(u => u.complete || u.unitClass.isBuilding).foreach(unit => {
      val color = unit.player.colorDark
      val text = Vector(
        unit.player.name,
        unit.unitClass.toString,
        if (unit.player == With.neutral) "" else unit.totalHealth.toString + "/" + unit.unitClass.maxTotalHealth,
        if (unit.complete) "" else "In progress"
      )
      DrawMap.labelBox(text, unit.pixel)
    })
  }
}
