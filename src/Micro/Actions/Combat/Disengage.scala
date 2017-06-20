package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Execution.ExecutionState

object Disengage extends Action {
  
  override protected def allowed(state: ExecutionState): Boolean = {
    state.unit.canMoveThisFrame
  }
  
  override protected def perform(state: ExecutionState) {
    
    // TODO: These outrange/outspeed concepts are excessively rigid.
    // For example, 20 dragoons vs 20 Vultures + 1 Siege Tank basically means WE OUTRANGE THEM even if one of their units outranges us.
    
    // Are we totally safe? Keep fighting.
    //
    if (state.threats.isEmpty) {
      Engage.delegate(state)
    }
  
    // Are we trapped? Fight like a caged animal.
    //
    val trapped = true // TODO
    if (trapped) {
      Engage.delegate(state)
    }
    
    // If we're faster than all the threats we can afford to be clever.
    //
    val weOutspeed = true // TODO
    val weOutrange = true // TODO
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
      Hover.consider(state)
    }
  
    // If we're outsped, we may be forced to fight
    val outsped = true
    if (outsped) {
      
      // Let's get some shots in, at least
      if (weOutrange) {
        Kite.delegate(state)
      }
    }
  
    // There's nothing clever to do. Just get out.
    Rout.consider(state)
  }
}
