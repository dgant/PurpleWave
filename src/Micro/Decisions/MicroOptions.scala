package Micro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MicroOptions {
  
  def choose(agent: FriendlyUnitInfo): MicroDecision = {
    val decisions = get(agent)
    val best      = decisions.maxBy(_.evaluation)
    agent.agent.microDecisions            = decisions
    agent.agent.microDecisionsUpdateFrame = With.frame
    best.best = true
    lazy val debuggingInfo = decisions.map(x => (x, "1.2f".format(x.valueFixed), "1.2f".format(x.valuePerFrame), "1.2f".format(x.evaluation))).sortBy(_._2)
    best
  }
  
  def evaluate(decision: MicroDecision): Double = {
    
    val frames = Math.max(With.latency.turnSize, Array(6.0, decision.frames, decision.agent.matchups.framesToLiveCurrently).min)
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
      travels(agent),
      cardinalMoves(agent)
    )
    val output = outputs.flatten
    output
  }
  
  def travels(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val zone = agent.zone
    val outputs = zone.edges
      .map(_.otherSideof(zone).centroid.pixelCenter)
      .map(pixel => DecideToMove(agent, pixel, moveFrames(agent)))
      .toVector
    outputs
  }
  
  def flee(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = agent.matchups.threats.map(threat => flee(agent, threat))
    val output  = outputs.flatten
    output
  }
  
  def cardinalMoves(agent: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = (0.0 until 256.0 by 256.0/8.0)
      .map(degrees => agent.pixelCenter.radiate256Degrees(degrees, 100.0))
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
  
  def describe(agent: FriendlyUnitInfo): String = {
    agent.agent.microDecisions.map(x => (x, "%1.2".format(x.evaluation))).sortBy(_._2).map(_.toString).mkString("\n")
  }
}
