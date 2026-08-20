package Micro.Agency

import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.UnitCommandType

object AttackDelay {

  /*
  Dragoon frames, via Ankmairdor:

  DragoonGndAttkInit:
    playfram           0x110    # frame set 16
    wait               1
    playfram           0x121    # frame set 17
    wait               1
    playfram           0x132    # frame set 18
    wait               1
    playfram           0x143    # frame set 19
    wait               1
    playfram           0x154    # frame set 20
    wait               1
    playfram           0x165    # frame set 21
    wait               1
  DragoonGndAttkRpt:
    playfram           0x176    # frame set 22
    wait               1
    playfram           0x187    # frame set 23
    wait               1
    attackwith         1
    playfram           0x176    # frame set 22
    wait               1
    playfram           0x165    # frame set 21
    gotorepeatattk
    ignorerest
   */

  
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
  
  def framesToWaitAfterIssuingAttackOrder(unit: FriendlyUnitInfo): Int = {
    if (Protoss.Carrier(unit)) {
      // Need a better answer than this
      return 24
    }
    // Sleep through command delivery so the next frame can observe and protect an attack that started.
    With.latency.remainingFrames + 1
  }
  
  // OpenBW applies commands before running iscripts. After nobrkcodestart has run, follow-up orders queue until
  // nobrkcodeend instead of interrupting the attack. These offsets are the first safe command-arrival frames,
  // relative to the frame on which the attack cooldown starts. Lurkers and Hydralisks use the end of their
  // interruptible repeat animation because cooldown alone does not distinguish an initial attack from a repeat.
  private lazy val safeOrderArrivalFramesByUnit = Map(
    Protoss.Corsair     -> 1,
    Zerg.SunkenColony   -> 1,
    Protoss.Arbiter     -> 2,
    Protoss.Archon      -> 2,
    Protoss.DarkTemplar -> 2,
    Terran.Valkyrie     -> 2,
    Zerg.Broodling      -> 2,
    Zerg.Ultralisk      -> 2,
    Zerg.Zergling       -> 2,
    Protoss.Zealot      -> 3,
    Zerg.Lurker         -> 3,
    Terran.Ghost        -> 4,
    Terran.Marine       -> 4,
    Zerg.Hydralisk      -> 4)

  private def awaitingAttackStart(unit: FriendlyUnitInfo): Boolean = {
    val lastCommand = unit.bwapiUnit.getLastCommand
    lastCommand != null &&
      lastCommand.getType == UnitCommandType.Attack_Unit &&
      unit.bwapiUnit.getLastCommandFrame > unit.lastFrameStartingAttack &&
      unit.orderTarget.exists(_.bwapiUnit == lastCommand.getTarget) &&
      unit.cooldownLeft <= With.latency.remainingFrames
  }

  def safeToIssueOrder(unit: FriendlyUnitInfo): Boolean = {
    // A delivered attack order can start after its sleep expires. Do not put a follow-up command in flight
    // unless cooldown guarantees that it will arrive before the pending attack can start.
    if (awaitingAttackStart(unit)) return false
    // The +2 is to make units stick less after an attack
    // I tested this prior to COG 2021.
    // +0 lead to a lot of sticking.
    // +1 lead to some sticking, enough to lose games because of it.
    // +2 appeared to prevent all sticking
    val safeArrivalFrame = unit.lastFrameStartingAttack + safeOrderArrivalFramesByUnit.getOrElse(unit.unitClass, unit.unitClass.stopFrames + 2)
    With.frame + With.latency.remainingFrames >= safeArrivalFrame
  }
  
  /*
  Here's the math justifying the fallback calculation.
  These examples assume turn size 1. The implementation uses remainingFrames to account for the current command phase.
  The scenarios show the unsafe +0 boundary; the fallback adds the empirically-tested two-frame margin.
  
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
  Once you see a unit starting to attack, issue a follow-up order only when its projected arrival is at least
  (Attack start frame + stop frames + 2).
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
  // The fallback above retains the additional empirical margin because consecutive attacks can otherwise miss.
  //
  // Reaching the earliest safe frame requires micro to run on *every frame*.
  //
}
