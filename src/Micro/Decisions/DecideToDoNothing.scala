package Micro.Decisions

import Debugging.Visualizations.Colors
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
    //With.commander.doNothing(agent)
  }
  
  override def renderMap() {
    renderWith(currentPixel, currentPixel, Colors.MediumGreen)
  }
  
}
