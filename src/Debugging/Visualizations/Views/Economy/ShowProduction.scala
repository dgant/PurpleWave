package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Planning.Plans.Macro.Build.ProductionPlan
import ProxyBwapi.Races.Zerg
import bwapi.Color

object ShowProduction extends View {

  case class Producible(name: String, framesTotal: Int, framesLeft: Int, colorBack: Color, colorFront: Color, started: Boolean = true)
  
  override def renderScreen() {
    val producibles: Seq[Producible] = With.units.ours.view.map(unit => {
      val unitClass = if (unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon)) unit.buildType else unit.unitClass
      if ( ! unit.complete)
        Some(Producible(
          unitClass.toString,
          unitClass.buildFrames,
          if (unit.complete) unit.remainingTrainFrames else unit.remainingCompletionFrames,
          Colors.DarkBlue,
          Colors.NeonBlue))
      else if (unit.teching)
        unit.techProducing.map(t => Producible(
          t.toString,
          t.researchFrames,
          unit.remainingTechFrames,
          Colors.MidnightGreen,
          Colors.BrightGreen))
      else if (unit.upgrading && unit.upgradeProducing.isDefined) {
        val up = unit.upgradeProducing.get
        val levelNext = 1 + With.self.getUpgradeLevel(up)
        Some(Producible(
          up.toString + (if (up.levels.length > 1) " " + levelNext.toString else ""),
          up.upgradeFrames(levelNext),
          unit.remainingUpgradeFrames,
          Colors.MidnightRed,
          Colors.MediumRed))
      }
      else None
    })
    .flatten
    .toVector ++ With.bank.requests
      .withFilter( ! _.isSpent)
      .withFilter(_.owner.isInstanceOf[ProductionPlan])
      .map(request => {
        val productionPlan = request.owner.asInstanceOf[ProductionPlan]
        val duration = if (productionPlan != null) productionPlan.buildable.frames else 24 * 45
        Producible(
          request.owner.toString
            .replaceAll("Morph a ", "")
            .replaceAll("Train a ", "")
            .replaceAll("Build a ", "")
            .replaceAll("Upgrade ", "")
            .replaceAll("Research ", "")
            .replaceAll(" 1", "")
          ,
          duration,
          Math.min(request.expectedFrames, duration),
          colorBack = Colors.ShadowGray,
          colorFront = Colors.ShadowGray,
          started = false
        )})

    producibles.sortBy(p => p.framesLeft - p.framesTotal).sortBy( ! _.started)
    producibles.indices.foreach(i => {
      val p = producibles(i)
      val y0 = 45 + 14 * i
      val y1 = y0 + 12
      val drawDenominator = () => With.game.drawBoxScreen(5, y0, 5 + p.framesTotal / 8,                   y1, p.colorBack,  p.started)
      val drawNumerator = () => With.game.drawBoxScreen(5, y0, 5 + (p.framesTotal - p.framesLeft) / 8,  y1, p.colorFront, true)
      if (p.started) {
        drawDenominator()
        drawNumerator()
      } else {
        drawNumerator()
        drawDenominator()
      }
      With.game.drawTextScreen(7, y0, p.name)
    })
  }
}
