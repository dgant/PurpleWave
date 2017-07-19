package Micro.Decisions

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Matchups.MatchupAnalysis
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class DecideToMove(
  argAgent        : FriendlyUnitInfo,
  destination     : Pixel,
  maxFrames       : Int,
  isCardinalMove  : Boolean = false)
    extends MicroDecision(argAgent) {
  
  lazy val hypotheticalMatchups: MatchupAnalysis = agent.matchups.ifAt(destination)
  
  override def valueFixed: Double = {
    val framesToExecute   = frames
    val nextStep          = agent.pixelCenter.project(destination, framesToExecute * agent.topSpeed)
    val valueLostHere     = framesToExecute / 2.0 * agent.matchups.vpfReceivingDiffused
    val valueLostThere    = framesToExecute / 2.0 * hypotheticalMatchups.vpfReceivingDiffused
    val net               = - valueLostHere - valueLostThere
    net
  }
  
  override def valuePerFrame: Double = {
    hypotheticalMatchups.netValuePerFrameDiffused - agent.matchups.netValuePerFrameDiffused
  }
  
  override def frames: Double = {
    Math.min(maxFrames, agent.framesToTravel(destination))
  }
  
  override def legal: Boolean = {
    destination.valid && agent.canTraverse(destination.tileIncluding)
  }
  
  override def execute() {
    With.commander.move(
      agent,
      if (isCardinalMove)
        destination
      else
        agent.pixelCenter.project(destination, 75.0)) // Less than 80 to get straight-line movement
  }
  
  override def renderMap() {
    renderWith(destination, destination, Colors.MediumBlue)
  }
  
}
