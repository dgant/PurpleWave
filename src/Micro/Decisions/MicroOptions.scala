package Micro.Decisions

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object MicroOptions {
  
  def get(actor: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = Vector(
      attacks(actor),
      moves(actor)
    )
    val output = outputs.flatten
    output
  }
  
  def attacks(actor: FriendlyUnitInfo): Vector[MicroDecision] = {
    val output = actor.matchups.targets.map(target => DecideToAttack(actor, target))
    output
  }
  
  def attack(actor: FriendlyUnitInfo, target: UnitInfo): Vector[MicroDecision] = {
    Vector(DecideToAttack(actor, target))
  }
  
  def moves(actor: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = Vector(
      flee(actor)
      // TODO: Add and evaluate traveling?
    )
    val output = outputs.flatten
    output
  }
  
  def flee(actor: FriendlyUnitInfo): Vector[MicroDecision] = {
    val outputs = actor.matchups.threats.map(threat => flee(actor, threat))
    val output  = outputs.flatten
    output
  }
  
  def flee(actor: FriendlyUnitInfo, threat: UnitInfo): Vector[MicroDecision] = {
    val output = Vector(
      DecideToMove(actor, threat.pixelCenter.project(actor.pixelCenter, threat.pixelRangeAgainstFromCenter(actor) + 32.0), moveFrames(actor))
    )
    output
  }
  
  def moveFrames(actor: FriendlyUnitInfo): Int = {
    Math.max(With.latency.turnSize, Math.min(actor.cooldownLeft, 6))
  }
}
