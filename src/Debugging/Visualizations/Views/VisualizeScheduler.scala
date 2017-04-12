package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawScreen
import Lifecycle.With

object VisualizeScheduler {
  
  def render() {
    With.game.drawTextScreen(25, 31, "Original queue")
    DrawScreen.table(25, 57,
      With.scheduler.queueOriginal
        .take(15)
        .map(buildable => Vector(buildable.toString)))
    
    With.game.drawTextScreen(175, 31, "Optimized queue")
    DrawScreen.table(175, 57,
      With.scheduler.simulationResults.suggestedEvents
        .toVector
        .sortBy(_.buildable.toString)
        .sortBy(_.frameEnd)
        .sortBy(_.frameStart)
        .take(15)
        .map(event => Vector(
          firstWord(event.toString),
          reframe(event.frameStart),
          reframe(event.frameEnd))))
    
    With.game.drawTextScreen(325, 31, "In progress")
    DrawScreen.table(325, 57,
      With.scheduler.simulationResults.simulatedEvents
        .filter(e => e.frameStart < With.frame)
        .toVector
        .sortBy(_.buildable.toString)
        .sortBy(_.frameStart)
        .sortBy(_.frameEnd)
        .take(15)
        .map(event => Vector(
          firstWord(event.toString),
          reframe(event.frameStart),
          reframe(event.frameEnd))))
    
    With.game.drawTextScreen(500, 31, "Impossible")
    DrawScreen.table(500, 57, With.scheduler.simulationResults.unbuildable
      .toVector
      .take(15)
      .map(buildable => Vector(buildable.toString.split("\\s+")(0))))
  }
  
  private def firstWord(text:String):String = text.split("\\s+")(0)
  
  private def reframe(frameAbsolute:Int):String = {
    val reframed = (frameAbsolute - With.frame)/24
    if (reframed <= 0) "Started" else reframed.toString
  }
}
