package Micro.Decisions

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class DecideToMove(agent: FriendlyUnitInfo, destination: Pixel, maxFrames: Int) extends MicroDecision {
  
  override def value: Double = {
    val framesToExecute   = frames
    val nextStep          = agent.pixelCenter.project(destination, framesToExecute * agent.topSpeed)
    val valueLostHere     = framesToExecute / 2.0 * agent.matchups.vpfReceivingDiffused
    val valueLostThere    = framesToExecute / 2.0 * agent.matchups.ifAt(destination).vpfReceivingDiffused
    val net               = - valueLostHere - valueLostThere
    net
  }
  
  override def frames: Double = {
    Math.min(maxFrames, agent.framesToTravel(destination))
  }
  
  override def execute(): Unit = {
    With.commander.move(agent, destination)
  }
}
