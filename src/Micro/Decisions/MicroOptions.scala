package Micro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MicroOptions {
  
  def choose(agent: FriendlyUnitInfo): MicroDecision = {
    agent.actionState.microDecisionsUpdateFrame = With.frame
    agent.actionState.microDecisions = get(agent)
    val output = agent.actionState.microDecisions.maxBy(_.evaluation)
    output
  }
  
  def evaluate(decision: MicroDecision): Double = {
    val frames = Math.min(48.0, decision.agent.matchups.framesToLiveCurrently)
    decision.valueFixed + decision.valuePerFrame * frames
  }
  
  def get(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = Vector(
      Vector(DecideToDoNothing(agent)),
      attacks(agent),
      moves(agent)
    )
    val output = outputs.flatten
    output
  }
  
  def attacks(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val output = agent.matchups.targets.map(target => DecideToAttack(agent, target))
    output
  }
  
  def attack(agent: FriendlyUnitInfo, target: UnitInfo): Vector[MicroDecision] = {
    Vector(DecideToAttack(agent, target))
  }
  
  def moves(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = Vector(
      flee(agent),
      cardinalMoves(agent)
    )
    val output = outputs.flatten
    output
  }
  
  def flee(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = agent.matchups.threats.map(threat => flee(agent, threat))
    val output  = outputs.flatten
    output
  }
  
  def cardinalMoves(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = (0.0 until 360.0 by 45.0)
      .map(degrees => agent.pixelCenter.radiateDegrees(degrees, 100.0))
      .map(destination => DecideToMove(agent, destination, moveFrames(agent), isCardinalMove = true))
      .toVector
    outputs
  }
  
  def flee(agent: FriendlyUnitInfo, threat: UnitInfo): Vector[MicroDecision] = {
    val output = Vector(
      DecideToMove(agent, threat.pixelCenter.project(agent.pixelCenter, threat.pixelRangeAgainstFromCenter(agent) + 32.0), moveFrames(agent))
    )
    output
  }
  
  def moveFrames(agent: FriendlyUnitInfo): Int = {
    Math.max(With.latency.turnSize, Math.min(agent.cooldownLeft, 6))
  }
}
