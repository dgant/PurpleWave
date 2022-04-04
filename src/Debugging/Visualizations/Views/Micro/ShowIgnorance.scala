package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Battles.BattleFilters
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowIgnorance extends View {

  override def renderMap() { With.units.all.foreach(renderUnit) }

  def renderUnit(unit: UnitInfo) {
    if ( ! With.viewport.contains(unit.pixel)) return
    if ( ! unit.alive) return
    if (unit.isNeutral) return
    if ( ! BattleFilters.local(unit)) return
    if (unit.battle.nonEmpty) return

    DrawMap.circle(unit.pixel, 6, Color.Black, solid = true)
    DrawMap.label("?", unit.pixel.add(2, 0))
  }

}
