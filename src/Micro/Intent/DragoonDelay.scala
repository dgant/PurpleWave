package Micro.Intent

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object DragoonDelay {
  
  def nextSafeFrameToOrder(dragoon: FriendlyUnitInfo): Int = {
    dragoon.lastAttackStartFrame + 1 + 9 - With.latency.framesRemaining
  }
  
  
  /*
  Update: June 2017
  
  PurpleWaveJadien: Alright. Let's solve this once and for all so I can die 200 years old and not have once thought about dragoon attack cancelling ever again
  PurpleWaveJadien: Frame 0: Dragoon isAttackStarting becomes true.
  PurpleWaveJadien: Frame X: Issue earliest possible move order without Dragoon shot failing or getting stuck
  PurpleWaveJadien: Goal is to solve for X
  jaj22: 7-latency probably
  PurpleWaveJadien: i... i don't believe you
  PurpleWaveJadien: or i'm just confused
  jaj22: I do need to test this again with order switch times  :P
  PurpleWaveJadien: why 7?
  PurpleWaveJadien: i thought we'd be talking 1 + 9 + max(2,3,4) = 14?
  jaj22: X is order issue time, not the time that it moves
  jaj22: and isStartingAttack goes true one frame after the attack order hits
  jaj22: hence you skip that 1 for a start
  PurpleWaveJadien: @jaj22 alright, so the timeline is:
  PurpleWaveJadien: -1: Attack order arrives from the network
  PurpleWaveJadien: 0: Attack order starts executing
  PurpleWaveJadien: isStartingAttack == true
  jaj22: and isAttackFrame = true, yes
  jaj22: 8: isAttackFrame is still true
  jaj22: 9: isAttackFrame goes false. Switching to move order at this point is safe.
  PurpleWaveJadien: @jaj22 ahah. So if latencyFrames is, say, 6, I can issue the order at frame... 4? and be okay?
  jaj22: latencyFramesRemaining  :P
  jaj22: but yeah, in theory
  jaj22: I'll check whether it's the move order or the moving that matters.
  jaj22: 9-gap, sticks  :D
  jaj22: 9 minus latency after isStartingAttack looks correct anyway
  jaj22: optimal number of frames to wait before firing your move order.
  jaj22: if the goon is changing direction then you can do 7 minus latency  :P
  PurpleWaveJadien: @jaj22 i... dear god what?
  jaj22: they only stick if you continue moving in the same direction after the shot.
  PurpleWaveJadien: i'm gonna pretend i never heard that
  jaj22: you can unstick them by giving a move order in the opposite direction too
  jaj22: you may have to wait for the move order to kick in  :P
  jaj22: hmm, unstick angle looks like <45 degrees
   */
  
  // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
  //
  // "Dragoon, Devourer only units that can have damage prevented by stop() too early"
  //
  // According to JohnJ: "After the frame where isStartingAttack is true, it should be left alone for the next 8 frames"
  // "I assume that frame count is in ignorance of latency, so if i issue an order before the 8 frames are up that would be executed on the first frame thereafter, i'm in the clear?"
  // JohnJ: yeah in theory. actually in practice. I did test that.
  //
  // JohnJ's research says (now + 9 - frames before execution) is the exact timing
  // That formula causes Dragoons to miss some shots, especially on consecutive attacks without moving.
  // So I've added one more frame of delay. Consecutive attacks are still sometimes missing.
  //
  // WARNING: This requires micro to run on *every frame*!
  //
}
