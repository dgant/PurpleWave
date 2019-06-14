package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With
import Planning.Plan
import Planning.Plans.Basic.NoPlan

object ShowPlans extends View {
  
  override def renderMap() {
    drawPlansRecursively(With.strategy.gameplan)
  }
  
  private def drawPlansRecursively(plan: Plan) {
    plan.visualize()
    plan.getChildren.filter(isRelevant).foreach(drawPlansRecursively)
  }

  var screenEnabled = false
  override def renderScreen() {
    if (screenEnabled) {
      describePlanTree(With.strategy.gameplan, 0, 0)
        .zipWithIndex
        .foreach(pair => With.game.drawTextScreen(0, (5 + pair._2) * With.visualization.lineHeightSmall, pair._1))
    }
  }
  
  private def describePlanTree(plan: Plan, childOrder: Integer, depth: Integer): Seq[String] = {
    if (isRelevant(plan))
      Vector(describePlan(plan, childOrder, depth)) ++
        plan.getChildren.zipWithIndex.flatten(x => describePlanTree(x._1, x._2, depth + 1))
    else Vector.empty
  }
  
  private def describePlan(plan: Plan, childOrder: Integer, depth: Integer): String = {
    val checkbox = if (plan.isComplete) "X " else "  "
    val spacer = "  " * depth
    val leftColumn =
      (checkbox
        ++ spacer
        ++ "#"
        ++ (childOrder + 1).toString
        ++ " "
        ++ plan.toString)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  private def isRelevant(plan: Plan): Boolean = {
    With.prioritizer.isPrioritized(plan) && ! plan.isComplete && plan != NoPlan()
  }
}
