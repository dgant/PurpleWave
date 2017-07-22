package Micro.Matchups

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}

case class MatchupAnalysis(us: UnitInfo, at: Pixel) {
 
  def this(us: UnitInfo) {
    this(us, us.pixelCenter)
  }
  
  def ifAt(elsewhere: Pixel): MatchupAnalysis = MatchupAnalysis(us, elsewhere)
  
  lazy val allies         : Vector[FriendlyUnitInfo]  = if (us.battle.isEmpty) Vector.empty else us.battle.get.us.units.filterNot(_ == us).flatMap(_.friendly)
  lazy val enemies        : Vector[ForeignUnitInfo]   = if (us.battle.isEmpty) Vector.empty else us.battle.get.enemy.units.flatMap(_.foreign)
  lazy val others         : Vector[UnitInfo]          = allies ++ enemies
  lazy val allUnits       : Vector[UnitInfo]          = others :+ us
  lazy val threats        : Vector[UnitInfo]  = enemies.filter(_.canAttackThisSecond(us))
  lazy val targets        : Vector[UnitInfo]  = enemies.filter(us.canAttackThisSecond)
  lazy val threatsViolent : Vector[UnitInfo]  = threats.filter(_.isBeingViolentTo(us))
  lazy val threatsInRange : Vector[UnitInfo]  = threats.filter(threat => threat.pixelRangeAgainstFromCenter(us) >= threat.pixelDistanceFast(at))
  lazy val targetsInRange : Vector[UnitInfo]  = targets.filter(target => us.pixelRangeAgainstFromCenter(target) >= target.pixelDistanceFast(at))
  lazy val vpfDealingDiffused         : Double = targetsInRange.map(target => dpfDealingDiffused(target)  * MicroValue.valuePerDamage(target)).sum
  lazy val vpfDealingCurrently        : Double = targetsInRange.map(target => dpfDealingCurrently(target) * MicroValue.valuePerDamage(target)).sum
  lazy val dpfReceivingDiffused       : Double = threatsInRange.map(_.matchups.dpfDealingDiffused(us)).sum
  lazy val dpfReceivingCurrently      : Double = threatsInRange.map(_.matchups.dpfDealingCurrently(us)).sum
  lazy val vpfReceivingDiffused       : Double = dpfReceivingDiffused   * MicroValue.valuePerDamage(us)
  lazy val vpfReceivingCurrently      : Double = dpfReceivingCurrently  * MicroValue.valuePerDamage(us)
  lazy val framesToLiveDiffused       : Double = PurpleMath.nanToInfinity(us.totalHealth / dpfReceivingDiffused)
  lazy val framesToLiveCurrently      : Double = PurpleMath.nanToInfinity(us.totalHealth / dpfReceivingCurrently)
  lazy val netValuePerFrameDiffused   : Double = vpfDealingDiffused   - vpfReceivingDiffused
  lazy val netValuePerFrameCurrently  : Double = vpfDealingCurrently  - vpfReceivingCurrently
  
  def dpfDealingDiffused(target: UnitInfo): Double = us.dpfAgainst(target) / Math.max(1.0, targets.size)
  def dpfDealingCurrently(target: UnitInfo): Double =
    if(us.target.contains(target))
      us.dpfAgainst(target)
    else if (us.target.isDefined)
      0.0
    else
      dpfDealingDiffused(target)
}
