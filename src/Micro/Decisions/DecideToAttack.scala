package Micro.Decisions

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

case class DecideToAttack(argAgent: FriendlyUnitInfo, target: UnitInfo) extends MicroDecision(argAgent) {
  
  val targetPixel: Pixel = target.pixelCenter
  val destination: Pixel = agent.pixelCenter.project(targetPixel, distanceToCover)
  
  lazy private val distanceNow      = agent.pixelDistanceEdge(target)
  lazy private val rangeAgainst     = agent.pixelRangeAgainstFromEdge(target)
  lazy private val distanceToCover  = Math.max(0.0,  distanceNow - rangeAgainst)
  
  override def valueFixed: Double = {
    val valueGained       = MicroValue.valuePerAttack(agent, target)
    val framesToExecute   = frames
    val valueLostHere     = framesToExecute / 2.0 * agent.matchups.vpfReceivingDiffused
    val valueLostThere    = framesToExecute / 2.0 * agent.matchups.ifAt(destination).vpfReceivingDiffused
    val net               = valueGained - valueLostHere - valueLostThere
    net
  }
  
  override def frames: Double = {
    agent.framesBeforeAttacking(target) + 1 + agent.unitClass.stopFrames
  }
  
  override def legal: Boolean = {
    agent.canAttack(target) && target.visible
  }
  
  override def execute(): Unit = {
    With.commander.attack(agent, target)
  }
  
  override def renderMap() {
    if (best) {
      renderWith(targetPixel, destination, Colors.MediumRed)
    }
  }
}
