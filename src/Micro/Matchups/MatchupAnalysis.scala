package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.{Battle, Team}
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{ByOption, Forever}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class MatchupAnalysis(me: UnitInfo, conditions: MatchupConditions) {
  
  def this(us: UnitInfo) {
    this(us, MatchupConditions(us.pixelCenter, 0))
  }
  
  lazy val at     : Pixel = conditions.at
  lazy val frame  : Int   = conditions.framesAhead
  
  lazy val hypotheticalMatchups = new mutable.HashMap[MatchupConditions, MatchupAnalysis]
  
  def ifAt(elsewhere: Pixel): MatchupAnalysis = ifAt(elsewhere, frame)
  def ifAt(framesAhead: Int): MatchupAnalysis = ifAt(at, framesAhead)
  private def ifAt(elsewhere: Pixel, framesAhead: Int): MatchupAnalysis = {
    val hypotheticalConditions = MatchupConditions(elsewhere, framesAhead)
    if ( ! hypotheticalMatchups.contains(hypotheticalConditions)) {
      hypotheticalMatchups.put(hypotheticalConditions, MatchupAnalysis(me, hypotheticalConditions))
    }
    hypotheticalMatchups(hypotheticalConditions)
  }
  
  lazy val battle                 : Option[Battle]        = me.battle.orElse(With.matchups.entrants.find(_._2.contains(me)).map(_._1))
  lazy val team                   : Option[Team]          = battle.map(_.teamOf(me))
  lazy val zoneUnits              : Vector[UnitInfo]      = me.zone.units.toVector.filter(BattleClassificationFilters.isEligibleLocal)
  lazy val enemies                : Vector[UnitInfo]      = team.map(_.opponent.units).getOrElse(zoneUnits.filter(_.isEnemyOf(me)))
  lazy val alliesIncludingSelf    : Vector[UnitInfo]      = team.map(_.units).getOrElse(zoneUnits.filter(u => u.isFriendly && u != me) :+ me)
  lazy val allies                 : Vector[UnitInfo]      = alliesIncludingSelf.filterNot(_.id == me.id)
  lazy val others                 : Vector[UnitInfo]      = enemies ++ allies
  lazy val allUnits               : Vector[UnitInfo]      = enemies ++ alliesIncludingSelf
  lazy val enemyDetectors         : Vector[UnitInfo]      = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val threats                : Vector[UnitInfo]      = enemies.filter(_.canAttack(me))
  lazy val targets                : Vector[UnitInfo]      = enemies.filter(me.canAttack)
  lazy val threatsViolent         : Vector[UnitInfo]      = threats.filter(_.isBeingViolentTo(me))
  lazy val threatsInRange         : Vector[UnitInfo]      = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val targetsInRange         : Vector[UnitInfo]      = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val repairers              : ArrayBuffer[UnitInfo] = ArrayBuffer.empty ++ allies.filter(ally => ally.is(Terran.SCV) && ally.target.contains(me))
  
  lazy val valuePerDamage                         : Double                = MicroValue.valuePerDamage(me)
  lazy val vpfDealingDiffused                     : Double                = targetsInRange.map(target => dpfDealingDiffused(target)  * target.matchups.valuePerDamage).sum
  lazy val vpfDealingCurrently                    : Double                = targetsInRange.map(target => dpfDealingCurrently(target) * target.matchups.valuePerDamage).sum
  lazy val dpfReceivingDiffused                   : Double                = threatsInRange.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val dpfReceivingCurrently                  : Double                = threatsInRange.map(_.matchups.dpfDealingCurrently(me)).sum
  lazy val vpfReceivingDiffused                   : Double                = valuePerDamage * dpfReceivingDiffused
  lazy val vpfReceivingCurrently                  : Double                = valuePerDamage * dpfReceivingCurrently
  lazy val vpfNetDiffused                         : Double                = vpfDealingDiffused   - vpfReceivingDiffused
  lazy val vpfNetCurrently                        : Double                = vpfDealingCurrently  - vpfReceivingCurrently
  lazy val framesToLiveDiffused                   : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceivingDiffused)
  lazy val framesToLiveCurrently                  : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceivingCurrently)
  lazy val doomedDiffused                         : Boolean               = framesToLiveDiffused <= framesToRetreatDiffused
  lazy val framesOfEntanglementPerThreatDiffused  : Map[UnitInfo, Double] = threats.map(threat => (threat, framesOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglementPerThreatCurrently : Map[UnitInfo, Double] = threatsViolent.map(threat => (threat, framesOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglementDiffused           : Double                = ByOption.max(framesOfEntanglementPerThreatDiffused.values).getOrElse(- Forever())
  lazy val framesOfEntanglementCurrently          : Double                = ByOption.max(framesOfEntanglementPerThreatCurrently.values).getOrElse(- Forever())
  lazy val framesOfSafetyDiffused                 : Double                = - With.latency.latencyFrames - With.reaction.agencyMax - ByOption.max(framesOfEntanglementPerThreatDiffused.values).getOrElse(- Forever())
  lazy val framesOfSafetyCurrently                : Double                = - With.latency.latencyFrames - With.reaction.agencyMax - ByOption.max(framesOfEntanglementPerThreatCurrently.values).getOrElse(- Forever())
  lazy val pixelsOfFreedom                        : Double                = if (me.flying) GameTime(1, 0)() else ByOption.min(others.filter( ! _.flying).map(_.pixelDistanceEdge(me))).getOrElse(GameTime(1, 0)())
  lazy val mostEntangledThreatsDiffused           : Vector[UnitInfo]      = threats.sortBy( - framesOfEntanglementPerThreatDiffused(_))
  lazy val mostEntangledThreatsCurrently          : Vector[UnitInfo]      = threats.sortBy( - framesOfEntanglementPerThreatCurrently(_))
  lazy val mostEntangledThreatDiffused            : Option[UnitInfo]      = ByOption.minBy(framesOfEntanglementPerThreatDiffused)(_._2).map(_._1)
  lazy val mostEntangledThreatCurrently           : Option[UnitInfo]      = ByOption.minBy(framesOfEntanglementPerThreatCurrently)(_._2).map(_._1)
  def framesToRetreatDiffused   : Double = Math.max(0.0, framesOfEntanglementDiffused)
  def framesToRetreatCurrently  : Double = Math.max(0.0, framesOfEntanglementCurrently)
  
  def dpfDealingDiffused  (target: UnitInfo): Double = me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)
  def dpfDealingCurrently (target: UnitInfo): Double =
    if ( ! me.canAttack)
      0.0
    else if(me.target.contains(target))
      me.dpfOnNextHitAgainst(target)
    else if (me.target.isDefined)
      0.0
    else
      dpfDealingDiffused(target)
  
  def framesOfEntanglementWith(threat: UnitInfo, fixedRange: Option[Double] = None): Double = {
    lazy val approachSpeedMe      = me.speedApproachingPixel(threat.pixelCenter)
    lazy val approachSpeedThreat  = threat.speedApproachingPixel(me.pixelCenter)
    lazy val approachSpeedTotal   = approachSpeedMe + approachSpeedThreat
    lazy val framesToTurn         = me.unitClass.framesToTurn180 // Should be this, but for performance limitations: me.unitClass.framesToTurn(me.angleRadians - threat.pixelCenter.radiansTo(me.pixelCenter))
    lazy val framesToAccelerate   = (me.topSpeed + approachSpeedThreat) / me.unitClass.accelerationFrames
    lazy val blastoffFrames       = if (me.unitClass.canMove) framesToTurn + framesToAccelerate else 0 //How long for us to turn around and run
    lazy val reactionFrames       = With.reaction.agencyMax + With.latency.framesRemaining
    lazy val threatRangeBonus     = if (threat.isFriendly) 0.0 else Math.max(0.0, approachSpeedTotal * reactionFrames)
    
    val effectiveRange = fixedRange.getOrElse(threat.pixelRangeAgainst(me) + threatRangeBonus)
    val gapPixels = me.pixelDistanceEdge(threat) - effectiveRange
    
    val gapSpeed          = if (gapPixels >= 0 && threat.canMove) threat.topSpeed else me.topSpeed
    val framesToCloseGap  = PurpleMath.nanToInfinity(Math.abs(gapPixels) / gapSpeed)
    val output            = framesToCloseGap * PurpleMath.signum( - gapPixels) + blastoffFrames
    
    output
  }
}
