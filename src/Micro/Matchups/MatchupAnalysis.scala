package Micro.Matchups

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.collection.mutable

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
  
  lazy val battle                 : Option[Battle]    = me.battle.orElse(With.matchups.entrants.find(_._2.contains(me)).map(_._1))
  lazy val enemies                : Vector[UnitInfo]  = if (battle.isEmpty) Vector.empty else battle.get.teamOf(me).opponent.units
  lazy val alliesIncludingSelf    : Vector[UnitInfo]  = if (battle.isEmpty) Vector.empty else battle.get.teamOf(me).units
  lazy val allies                 : Vector[UnitInfo]  = alliesIncludingSelf.filterNot(_.id == me.id)
  lazy val others                 : Vector[UnitInfo]  = enemies ++ allies
  lazy val allUnits               : Vector[UnitInfo]  = enemies ++ alliesIncludingSelf
  lazy val enemyDetectors         : Vector[UnitInfo]  = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val threats                : Vector[UnitInfo]  = enemies.filter(_.canAttack(me))
  lazy val targets                : Vector[UnitInfo]  = enemies.filter(me.canAttack)
  lazy val threatsViolent         : Vector[UnitInfo]  = threats.filter(_.isBeingViolentTo(me))
  lazy val threatsInRange         : Vector[UnitInfo]  = threats.filter(threat => threat.pixelRangeAgainstFromCenter(me) >= threat.pixelDistanceFast(at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val threatsViolentInRange  : Vector[UnitInfo]  = threatsInRange.filter(_.isBeingViolentTo(me))
  lazy val targetsInRange         : Vector[UnitInfo]  = targets.filter(target => target.visible && me.pixelRangeAgainstFromCenter(target) >= target.pixelDistanceFast(at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame))
  
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
  lazy val framesOfEntanglementDiffused           : Double                = ByOption.min(framesOfEntanglementPerThreatDiffused.values).getOrElse(Double.NegativeInfinity)
  lazy val framesOfEntanglementCurrently          : Double                = ByOption.min(framesOfEntanglementPerThreatCurrently.values).getOrElse(Double.NegativeInfinity)
  lazy val framesOfSafetyDiffused                 : Double                = - With.reaction.agencyAverage - ByOption.max(framesOfEntanglementPerThreatDiffused.values).getOrElse(Double.NegativeInfinity)
  lazy val framesOfSafetyCurrently                : Double                = - With.reaction.agencyAverage - ByOption.max(framesOfEntanglementPerThreatCurrently.values).getOrElse(Double.NegativeInfinity)
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
  
  def framesOfEntanglementWith(threat: UnitInfo): Double = {
    val pixelsWithinRange = threat.pixelRangeAgainstFromCenter(me) - me.pixelDistanceFast(threat)
    val frames            = me.framesToTravelPixels(Math.abs(pixelsWithinRange))
    val output            = frames * (if (pixelsWithinRange < 0) -1.0 else 1.0)
    output
  }
}
