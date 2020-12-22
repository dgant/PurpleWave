package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Views.View
import Information.Battles.Types.NewBattleJudgment

object ShowJudgment extends View {
  override def renderScreen(): Unit = {
    ShowBattles.localBattle.flatMap(_.judgement).foreach(render)
  }

  private def render(judgment: NewBattleJudgment): Unit = {
    val startX = 580
    val startY = 40

  }
}
