package Debugging.Visualizations

import Lifecycle.With

class Animation {
  def durationFrames: Int = 240
  def drawScreen(): Unit = {}
  def drawMap(): Unit = {}

  final val frameCreated = With.frame
  final def age: Int = With.framesSince(frameCreated)
}
