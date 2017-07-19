package Micro.Decisions

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class DecideToDoNothing(argAgent: FriendlyUnitInfo) extends MicroDecision(argAgent) {
  
  override def valueFixed: Double = {
    0.0
  }
  
  override def valuePerFrame: Double = {
    agent.matchups.netValuePerFrameDiffused
  }
  
  override def frames: Double = {
    With.latency.turnSize
  }
  
  override def legal: Boolean = {
    true
  }
  
  override def execute() {
    With.commander.doNothing(agent)
  }
  
  override def renderMap() {
    DrawMap.label("%1.2f".format(valuePerFrame), currentPixel, backgroundColor = Colors.MediumGreen)
  }
  
}
