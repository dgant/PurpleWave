package Debugging.Visualizations

import Lifecycle.With

import scala.collection.mutable.ListBuffer

class Animations {

  private val animations: ListBuffer[Animation]  = new ListBuffer[Animation]()

  def add(animation: Animation): Unit = {
    animations += animation
  }

  def addMap(animate: () => Unit, duration: Int = 96): Unit = {
    add(new Animation {
      override def drawMap(): Unit = animate()
      override def durationFrames: Int = duration
    })
  }

  def addScreen(animate: () => Unit, duration: Int = 96): Unit = {
    add(new Animation {
      override def drawScreen(): Unit = animate()
      override def durationFrames: Int = duration
    })
  }

  def render(): Unit = {
    animations --= animations.filter(a => a.age > a.durationFrames)
    if (With.visualization.enabled) {
      if (With.visualization.map) {
        animations.foreach(_.drawMap())
      }
      if (With.visualization.screen) {
        animations.foreach(_.drawScreen())
      }
    }

  }
}
