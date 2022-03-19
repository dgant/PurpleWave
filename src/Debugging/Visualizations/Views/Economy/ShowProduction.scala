package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Tactics.Production.Production
import bwapi.Color

object ShowProduction extends View {

  case class Producible(name: String, framesTotal: Int, framesLeft: Int, colorDenominator: Color, colorNumerator: Color, started: Boolean = true, paid: Boolean = true)

  override def renderScreen() {
    val producibles: Seq[Producible] = With.units.ours.view.map(unit => {
      val unitClass = if (unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon)) unit.buildType else unit.unitClass
      if ( ! unit.complete)
        Some(Producible(
          unitClass.toString,
          unitClass.buildFrames,
          if (unit.complete) unit.remainingTrainFrames else unit.remainingCompletionFrames,
          if (unit.friendly.forall(_.producer.isEmpty)) Colors.DarkRed else Colors.DarkBlue,
          if (unit.friendly.forall(_.producer.isEmpty)) Colors.NeonRed else Colors.NeonBlue))
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
          Colors.MidnightViolet,
          Colors.MediumViolet))
      }
      else None
    })
    .flatten
    .toVector ++ (if (With.bank.requests.isEmpty) With.bank.requestsLast else With.bank.requests)
      .withFilter( ! _.isSpent)
      .withFilter(_.owner.isInstanceOf[Production])
      .map(request => {
        val productionPlan = request.owner.asInstanceOf[Production]
        val duration = if (productionPlan != null) productionPlan.buildable.buildFrames else 24 * 45
        Producible(
          request.owner.toString.replaceAll("Produce ", ""),
          duration,
          Math.min(request.expectedFrames, duration),
          colorDenominator = Colors.ShadowGray,
          colorNumerator = Colors.ShadowGray,
          started = false,
          paid = With.bank.requests.view.filter(_.owner == productionPlan).forall(_.isSatisfied)
        )})

    producibles.sortBy(p => p.framesLeft - p.framesTotal).sortBy( ! _.started)
    producibles.indices.foreach(i => {
      val p = producibles(i)
      val y0 = 45 + 14 * i
      val y1 = y0 + 12
      val colorDenominator = if (p.paid && ! p.started) Colors.BrightGray else p.colorDenominator
      val drawDenominator = () => With.game.drawBoxScreen(5, y0, 5 + p.framesTotal / 8,                 y1, colorDenominator, p.started)
      val drawNumerator = () => With.game.drawBoxScreen(5, y0, 5 + (p.framesTotal - p.framesLeft) / 8,  y1, p.colorNumerator, true)
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
