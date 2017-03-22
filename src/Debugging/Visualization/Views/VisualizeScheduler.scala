package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawScreen
import Startup.With

object VisualizeScheduler {
  
  def render() {
    With.game.drawTextScreen(25, 31, "Original queue")
    DrawScreen.table(25, 57,
      With.scheduler.queueOriginal
        .take(15)
        .map(buildable => List(buildable.toString)))
    
    With.game.drawTextScreen(175, 31, "Optimized queue")
    DrawScreen.table(175, 57,
      With.scheduler.simulationResults.suggestedEvents
        .toList
        .sortBy(_.buildable.toString)
        .sortBy(_.frameEnd)
        .sortBy(_.frameStart)
        .take(15)
        .map(event => List(
          firstWord(event.toString),
          reframe(event.frameStart),
          reframe(event.frameEnd))))
    
    With.game.drawTextScreen(325, 31, "In progress")
    DrawScreen.table(325, 57,
      With.scheduler.simulationResults.simulatedEvents
        .filter(e => e.frameStart < With.frame)
        .toList
        .sortBy(_.buildable.toString)
        .sortBy(_.frameStart)
        .sortBy(_.frameEnd)
        .take(15)
        .map(event => List(
          firstWord(event.toString),
          reframe(event.frameStart),
          reframe(event.frameEnd))))
    
    With.game.drawTextScreen(500, 31, "Impossible")
    DrawScreen.table(500, 57, With.scheduler.simulationResults.unbuildable
      .toList
      .take(15)
      .map(buildable => List(buildable.toString.split("\\s+")(0))))
  }
  
  private def firstWord(text:String):String = text.split("\\s+")(0)
  
  private def reframe(frameAbsolute:Int):String = {
    val reframed = (frameAbsolute - With.frame)/24
    if (reframed <= 0) "Started" else reframed.toString
  }
}
