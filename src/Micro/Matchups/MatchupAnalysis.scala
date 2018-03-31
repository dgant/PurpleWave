package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.{Battle, Team}
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{ByOption, Forever}

import scala.collection.mutable.ArrayBuffer

case class MatchupAnalysis(me: UnitInfo, conditions: MatchupConditions) {
  
  def this(us: UnitInfo) {
    this(us, MatchupConditions(us.pixelCenter, 0))
  }
  
  lazy val at     : Pixel = conditions.at
  lazy val frame  : Int   = conditions.framesAhead
  
  // Default is necessary for killing empty bases because no Battle is happening
  lazy val defaultUnits           : Vector[UnitInfo]      = if (me.canAttack) me.zone.units.toVector.filter(u => u.isEnemy && BattleClassificationFilters.isEligibleLocal(u)) else Vector.empty
  lazy val battle                 : Option[Battle]        = me.battle.orElse(With.matchups.entrants.find(_._2.contains(me)).map(_._1))
  lazy val team                   : Option[Team]          = battle.map(_.teamOf(me))
  lazy val enemies                : Vector[UnitInfo]      = team.map(_.opponent.units).getOrElse(defaultUnits.filter(_.isEnemyOf(me)))
  lazy val alliesInclSelf         : Vector[UnitInfo]      = team.map(_.units).getOrElse(defaultUnits.filter(u => u.isFriendly && u != me) :+ me)
  lazy val alliesInclSelfCloaked  : Vector[UnitInfo]      = alliesInclSelf.filter(_.cloaked)
  lazy val allies                 : Vector[UnitInfo]      = alliesInclSelf.filterNot(_.id == me.id)
  lazy val others                 : Vector[UnitInfo]      = enemies ++ allies
  lazy val allUnits               : Vector[UnitInfo]      = enemies ++ alliesInclSelf
  lazy val enemyDetectors         : Vector[UnitInfo]      = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val threats                : Vector[UnitInfo]      = enemies.filter(_.canAttack(me))
  lazy val targets                : Vector[UnitInfo]      = enemies.filter(me.canAttack)
  lazy val threatsViolent         : Vector[UnitInfo]      = threats.filter(_.isBeingViolentTo(me))
  lazy val threatsInRange         : Vector[UnitInfo]      = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val targetsInRange         : Vector[UnitInfo]      = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  
  def repairers: ArrayBuffer[UnitInfo] = ArrayBuffer.empty ++ allies.filter(_.friendly.exists(_.agent.toRepair.contains(me)))
  
  lazy val valuePerDamage                 : Double                = MicroValue.valuePerDamage(me)
  lazy val vpfDealing                     : Double                = targetsInRange.map(target => dpfDealingDiffused(target)  * target.matchups.valuePerDamage).sum
  lazy val dpfReceiving                   : Double                = threatsInRange.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val vpfReceiving                   : Double                = valuePerDamage * dpfReceiving
  lazy val vpfNet                         : Double                = vpfDealing   - vpfReceiving
  lazy val framesToLive                   : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceiving)
  lazy val doomed                         : Boolean               = framesToLive <= framesOfEntanglement
  lazy val framesOfEntanglementPerThreat  : Map[UnitInfo, Double] = threats.map(threat => (threat, framesOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglement           : Double                = ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever())
  lazy val framesOfSafety                 : Double                = - With.latency.latencyFrames - With.reaction.agencyMax - ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever())
  
  def dpfDealingDiffused  (target: UnitInfo): Double = me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)
  
  def framesOfEntanglementWith(threat: UnitInfo, fixedRange: Option[Double] = None): Double = {
    lazy val approachSpeedMe      = me.speedApproaching(threat.pixelCenter)
    lazy val approachSpeedThreat  = if (threat.is(Protoss.Interceptor)) 0.0 else threat.speedApproaching(me.pixelCenter)
    lazy val approachSpeedTotal   = approachSpeedMe + approachSpeedThreat
    lazy val framesToTurn         = me.unitClass.framesToTurn180 // Should be this, but for performance limitations: me.unitClass.framesToTurn(me.angleRadians - threat.pixelCenter.radiansTo(me.pixelCenter))
    lazy val framesToAccelerate   = (me.topSpeed + approachSpeedMe + approachSpeedThreat) / me.unitClass.accelerationFrames
    lazy val blastoffFrames       = if (me.unitClass.canMove) framesToTurn + framesToAccelerate else 0 //How long for us to turn around and run
    lazy val reactionFrames       = With.reaction.agencyMax + 2 * With.latency.framesRemaining
    lazy val threatRangeBonus     = if (threat.isFriendly) 0.0 else Math.max(0.0, approachSpeedTotal * reactionFrames)
    
    val effectiveRange = fixedRange.getOrElse(threat.pixelRangeAgainst(me) + threatRangeBonus)
    val gapPixels = me.pixelDistanceEdge(threat) - effectiveRange
    
    val gapSpeed          = if (gapPixels >= 0 && threat.canMove) threat.topSpeed else me.topSpeed
    val framesToCloseGap  = PurpleMath.nanToInfinity(Math.abs(gapPixels) / gapSpeed)
    val output            = framesToCloseGap * PurpleMath.signum( - gapPixels) + blastoffFrames
    
    output
  }
}
