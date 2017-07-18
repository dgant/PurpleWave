package Micro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

case class DecideToAttack(agent: FriendlyUnitInfo, target: UnitInfo) extends MicroDecision {
  
  override def value: Double = {
    val distanceNow       = agent.pixelsFromEdgeFast(target)
    val rangeAgainst      = agent.pixelRangeAgainstFromEdge(target)
    val distanceToCover   = Math.max(0.0,  distanceNow - rangeAgainst)
    val destination       = agent.pixelCenter.project(target.pixelCenter, distanceToCover)
    val valueGained       = MicroValue.valuePerAttack(agent, target)
    val framesToExecute   = frames
    val valueLostHere     = framesToExecute / 2.0 * agent.matchups.vpfReceivingDiffused
    val valueLostThere    = framesToExecute / 2.0 * agent.matchups.ifAt(destination).vpfReceivingDiffused
    val net               = valueGained - valueLostHere - valueLostThere
    net
  }
  
  override def frames: Double = {
    agent.framesBeforeAttacking(target) + agent.unitClass.stopFrames
  }
  
  override def execute(): Unit = {
    With.commander.attack(agent, target)
  }
}
