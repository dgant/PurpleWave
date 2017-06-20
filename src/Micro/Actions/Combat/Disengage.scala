package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Disengage extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame
  }
  
  override protected def perform(state: ExecutionState) {
    
    val completelySafe = state.threats.isEmpty
    if (completelySafe) {
      Engage.delegate(state)
      return
    }
    
    val trapped = state.threats.count(threat =>
      threat.melee
      && threat.topSpeed > state.unit.topSpeed
      && threat.pixelDistanceFast(state.unit) < 48.0) > 2
    if (trapped) {
      Brawl.delegate(state)
      Engage.delegate(state)
    }
  
    // TODO: These outrange/outspeed concepts are excessively rigid.
    // For example, 20 dragoons vs 20 Vultures + 1 Siege Tank basically means WE OUTRANGE THEM even if one of their units outranges us.
    
    // If we're faster than all the threats we can afford to be clever.
    //
    val ourMaxRange     = if (state.targets.isEmpty) 0.0 else state.unit.pixelRangeMax
    val threatMaxSpeed  = state.threats.map(_.topSpeed).max
    val threatMaxRange  = state.threats.map(_.pixelRangeAgainstFromCenter(state.unit)).max
    val weOutspeed      = state.unit.topSpeed > threatMaxSpeed
    val weOutrange      = ourMaxRange > threatMaxRange
    if (weOutspeed) {
      // Do we outrange AND outspeed all threats? MAYBE Kite.
      // But don't, for example, kite 10 Marines with a Dragoon.
      //
      if (weOutrange) {
        val kitingIsSmart = true // TODO
        if (kitingIsSmart) {
          Kite.delegate(state)
        }
      }
      HoverOutsideRange.consider(state)
    }
  
    // If we're caught, we may be forced to fight
    val caught = state.threats.exists(threat => threat.inRangeToAttackFast(state.unit) && threat.topSpeed > state.unit.topSpeed)
    if (caught) {
      // Let's get some shots in, at least
      if (weOutrange) {
        Kite.delegate(state)
      }
      else {
        Potshot.delegate(state)
      }
    }
  
    // There's nothing clever to do. Just get out.
    Rout.consider(state)
  }
}
