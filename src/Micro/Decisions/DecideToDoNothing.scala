package Micro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import sun.management.resources.agent

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
  
}
