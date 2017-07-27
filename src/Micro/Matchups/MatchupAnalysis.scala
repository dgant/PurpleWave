package Micro.Matchups

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

case class MatchupAnalysis(
                            me: UnitInfo,
                            conditions: MatchupConditions) {
 
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
  
  lazy val allies         : Vector[UnitInfo]  = if (me.battle.isEmpty) Vector.empty else me.battle.get.teamOf(me).units.filterNot(_ == me)
  lazy val enemies        : Vector[UnitInfo]  = if (me.battle.isEmpty) Vector.empty else me.battle.get.teamOf(me).opponent.units
  lazy val others         : Vector[UnitInfo]  = allies ++ enemies
  lazy val allUnits       : Vector[UnitInfo]  = others :+ me
  lazy val threats        : Vector[UnitInfo]  = enemies.filter(_.canAttack(me))
  lazy val targets        : Vector[UnitInfo]  = enemies.filter(me.canAttack)
  lazy val threatsViolent : Vector[UnitInfo]  = threats.filter(_.isBeingViolentTo(me))
  lazy val threatsInRange : Vector[UnitInfo]  = threats.filter(threat => threat.pixelRangeAgainstFromCenter(me) >= threat.pixelDistanceFast(at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val targetsInRange : Vector[UnitInfo]  = targets.filter(target => me.pixelRangeAgainstFromCenter(target) >= target.pixelDistanceFast(at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame))
   
  lazy val vpfDealingDiffused         : Double  = targetsInRange.map(target => dpfDealingDiffused(target)  * MicroValue.valuePerDamage(target)).sum
  lazy val vpfDealingCurrently        : Double  = targetsInRange.map(target => dpfDealingCurrently(target) * MicroValue.valuePerDamage(target)).sum
  lazy val dpfReceivingDiffused       : Double  = threatsInRange.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val dpfReceivingCurrently      : Double  = threatsInRange.map(_.matchups.dpfDealingCurrently(me)).sum
  lazy val vpfReceivingDiffused       : Double  = dpfReceivingDiffused   * MicroValue.valuePerDamage(me)
  lazy val vpfReceivingCurrently      : Double  = dpfReceivingCurrently  * MicroValue.valuePerDamage(me)
  lazy val netValuePerFrameDiffused   : Double  = vpfDealingDiffused   - vpfReceivingDiffused
  lazy val netValuePerFrameCurrently  : Double  = vpfDealingCurrently  - vpfReceivingCurrently
  lazy val framesToLiveDiffused       : Double  = PurpleMath.nanToInfinity(me.totalHealth / dpfReceivingDiffused)
  lazy val framesToLiveCurrently      : Double  = PurpleMath.nanToInfinity(me.totalHealth / dpfReceivingCurrently)
  lazy val framesToRetreatDiffused    : Double  = if (threatsInRange.isEmpty) 0.0 else threatsInRange.map(threat => me.framesToTravelPixels(threat.pixelRangeAgainstFromCenter(me) - threat.pixelDistanceFast(at))).max
  lazy val doomed                     : Boolean = framesToLiveDiffused <= framesToRetreatDiffused
  
  def dpfDealingDiffused  (target: UnitInfo): Double = me.dpfAgainst(target) / Math.max(1.0, targets.size)
  def dpfDealingCurrently (target: UnitInfo): Double =
    if(me.target.contains(target))
      me.dpfAgainst(target)
    else if (me.target.isDefined)
      0.0
    else
      dpfDealingDiffused(target)
}
