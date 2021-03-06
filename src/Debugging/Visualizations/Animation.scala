package Debugging.Visualizations

import Lifecycle.With

class Animation {
  def durationFrames: Int = 96
  def drawScreen(): Unit = {}
  def drawMap(): Unit = {}

  final val frameCreated = With.frame
  final def age: Int = With.framesSince(frameCreated)
}
