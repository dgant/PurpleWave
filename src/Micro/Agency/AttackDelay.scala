package Micro.Agency

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object AttackDelay {
  
  /*
  UPDATE 8/7/0217
  Resident wizard jaj22 discovers a new way to make Dragoons stick:
  
  jaj22: goon chasing zealot on attack order. Give attack order against different zealot in range. Goon fires and sticks.
   */
  
  /*
  Units get stuck when they execute a move order shortly after their attack animation finishes.
  The goal here is to withhold orders until the unit can safely receive them.
  
  In practice it's probably impossible to fully prevent stuck units without having frame-perfect movement and range calculations.
  Units will start attacks at times you don't expect.
  It'll fool you into issuing an order when you think it's safe but which after latency causes the unit to stick.
  So having good stuck-unit detection and unsticking is also important.
  */
  
  private val inexplicableExperimentalSafetyMargin = 1
  
  def framesToWaitAfterIssuingAttackOrder(unit: FriendlyUnitInfo): Int = {
    if (unit.is(Protoss.Carrier)) {
      // Need a better answer than this
      return 24
    }
    1 + Math.max(With.latency.latencyFrames, unit.unitClass.stopFrames)                 + inexplicableExperimentalSafetyMargin
  }
  
  def nextSafeOrderFrame(unit: FriendlyUnitInfo): Int = {
    if (unit.is(Protoss.Dragoon))
      nextSafeOrderFrameDragoon(unit)
    else
      nextSafeOrderFrameInGeneral(unit)
  }
  
  private def nextSafeOrderFrameInGeneral(unit: FriendlyUnitInfo): Int = {
    unit.lastFrameStartingAttack + unit.unitClass.stopFrames - With.latency.latencyFrames  + inexplicableExperimentalSafetyMargin
  }
  
  private def nextSafeOrderFrameDragoon(dragoon: FriendlyUnitInfo): Int = {
    dragoon.lastFrameStartingAttack + 1 + 9 - With.latency.latencyFrames                   + inexplicableExperimentalSafetyMargin
  }
  
  /*
  Here's the math justifying the above calculations.
  This math assumes turn size 1. Turn sizes in most cases are 1 (Local PC) or 2 (LAN).
  For some turn size T > 1 you could be a bit more precise and save N - 1 frames.
  
  ---

  Scenario 1:
  Latency frames: 5
  Stop frames: 	  10
  
  Frame 0: Issue attack order
  Frame 5: Receive attack order
  Frame 6: Execute attack order <-- Attack animation starts
  Frame 11: Issue move order    <-- Attack start frame (6) + stop frames (10) - latency frames (5)
  Frame 15: Last stopped frame.
  Frame 16: Receive move order. If we started moving now we would get stuck.
  Frame 17: Start moving
  
  ---
  
  Scenario 2:
  Latency frames: 5
  Stop frames:	  2
  
  Frame 0: Issue attack order
  Frame 3; Issue move order
  Frame 5: Receive attack order
  Frame 6: Execute attack order
  Frame 7: Last stopped frame
  Frame 8: Receive move order. If we started moving now we would get stuck.
  Frame 9: Start moving
  
  ---
  
  Conclusion:
  If you issue an attack order on frame N,
  The next safe time to issue a move order to the unit is 1 + Max(Latency frames, stop frames)
  And once you see a unit starting to attack, withhold orders until (Attack start frame + stop frames - latency frames)
   */
  
  /*
  Notes from June 2017
  
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
