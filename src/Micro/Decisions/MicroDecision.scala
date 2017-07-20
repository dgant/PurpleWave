package Micro.Decisions

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Color

abstract class MicroDecision(actingAgent: FriendlyUnitInfo) {
  
  var best: Boolean = false
  
  def agent: FriendlyUnitInfo = actingAgent
  
  def valueFixed: Double = {
    0.0
  }
  
  def valuePerFrame: Double = {
    agent.matchups.netValuePerFrameDiffused
  }
  
  def frames: Double
  def legal: Boolean
  def execute()
  def renderMap() {}
  
  lazy val evaluation: Double = MicroOptions.evaluate(this)
  val currentPixel = actingAgent.pixelCenter
  
  protected def renderWith(lineTo: Pixel, labelAt: Pixel, color: Color) {
    if (best) {
      DrawMap.circle(labelAt, 8, color, solid = true)
    }
    DrawMap.line(currentPixel, lineTo, color)
    DrawMap.label("%1.2f".format(evaluation), labelAt, drawBackground = true, backgroundColor = color)
  }
}
