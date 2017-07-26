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
  
  override def valuePerFrame: Double = {
    (agent.matchups.netValuePerFrameDiffused + hypotheticalMatchups.netValuePerFrameDiffused) / 2.0
  }
  
  override def frames: Double = {
    Math.min(maxFrames, agent.framesToTravelTo(destination))
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
