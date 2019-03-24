package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import Micro.Heuristics.Targeting.TargetHeuristicVpfEnemy
import ProxyBwapi.Races.{Protoss, Zerg}
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
  private lazy val defaultUnits   : Vector[UnitInfo]    = if (me.canAttack) me.zone.units.filter(u => u.isEnemy && BattleClassificationFilters.isEligibleLocal(u)) else Vector.empty
  lazy val battle                 : Option[BattleLocal] = me.battle.orElse(With.matchups.entrants.find(_._2.contains(me)).map(_._1))
  lazy val allUnits               : Vector[UnitInfo]    = battle.map(b => b.teams.flatMap(_.units)    ++ With.matchups.entrants.getOrElse(b, Set.empty)).getOrElse(defaultUnits).distinct
  lazy val enemies                : Vector[UnitInfo]    = battle.map(b => b.teamOf(me).opponent.units ++ With.matchups.entrants.getOrElse(b, Set.empty).filter(   _.isEnemyOf(me))).getOrElse(defaultUnits).distinct
  lazy val alliesInclSelf         : Vector[UnitInfo]    = battle.map(b => b.teamOf(me).units          ++ With.matchups.entrants.getOrElse(b, Set.empty).filter( ! _.isEnemyOf(me))).getOrElse(Vector.empty).distinct
  lazy val alliesInclSelfCloaked  : Vector[UnitInfo]    = alliesInclSelf.filter(_.cloakedOrBurrowed)
  lazy val allies                 : Vector[UnitInfo]    = alliesInclSelf.filterNot(_.id == me.id)
  lazy val others                 : Vector[UnitInfo]    = enemies ++ allies
  lazy val allyDetectors          : Vector[UnitInfo]    = allies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val enemyDetectors         : Vector[UnitInfo]    = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val threats                : Vector[UnitInfo]    = enemies.filter(threatens(_, me))
  lazy val targets                : Vector[UnitInfo]    = enemies.filter(threatens(me, _))
  lazy val threatsViolent         : Vector[UnitInfo]    = threats.filter(_.isBeingViolentTo(me))
  lazy val threatsInRange         : Vector[UnitInfo]    = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val targetsInRange         : Vector[UnitInfo]    = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val nearestArbiter         : Option[UnitInfo]    = ByOption.minBy(allies.view.filter(_.is(Protoss.Arbiter)))(_.pixelDistanceSquared(me))
  
  private def threatens(shooter: UnitInfo, victim: UnitInfo): Boolean = {
    if ( ! shooter.canAttack(victim)) return false
    true
  }
  def repairers: ArrayBuffer[UnitInfo] = ArrayBuffer.empty ++ allies.filter(_.friendly.exists(_.agent.toRepair.contains(me)))
  
  lazy val valuePerDamage                 : Double                = MicroValue.valuePerDamageCurrentHp(me)
  lazy val dpfDealingMax                  : Double                = splashFactorMax * ByOption.max(targets.view.map(me.dpfOnNextHitAgainst)).getOrElse(0.0)
  lazy val vpfDealingMax                  : Double                = splashFactorMax * ByOption.max(targets.view.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val vpfDealingInRange              : Double                = splashFactorInRange * ByOption.max(targetsInRange.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val dpfReceiving                   : Double                = threatsInRange.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val vpfReceiving                   : Double                = valuePerDamage * dpfReceiving
  lazy val vpfNet                         : Double                = vpfDealingInRange - vpfReceiving
  lazy val vpfTargetHeuristic             : Double                = TargetHeuristicVpfEnemy.calculate(me)
  lazy val framesBeforeAttacking          : Double                = ByOption.max(targets.view.map(me.framesBeforeAttacking)).getOrElse(Forever()).toDouble
  lazy val framesToLive                   : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceiving)
  lazy val doomed                         : Boolean               = framesToLive <= framesOfEntanglement
  lazy val framesOfEntanglementPerThreat  : Map[UnitInfo, Double] = threats.map(threat => (threat, framesOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglement           : Double                = ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever())
  lazy val framesOfSafety                 : Double                = - With.latency.latencyFrames - With.reaction.agencyAverage - ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever().toDouble)
  lazy val teamFramesOfSafety             : Double                = ByOption.min(alliesInclSelf.view.map(_.matchups.framesOfSafety)).getOrElse(0)

  def dpfDealingDiffused(target: UnitInfo): Double = splashFactorInRange * me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)
  
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
  
  lazy val splashFactorMax: Double = splashFactorForUnits(targets)
  lazy val splashFactorInRange: Double = splashFactorForUnits(targetsInRange)
  
  protected def splashFactorForUnits(targetsConsidered: Iterable[UnitInfo]): Double = {
    val min = targetsConsidered.size
    val max =
      if(me.unitClass.dealsRadialSplashDamage || me.is(Zerg.Lurker))
        2.5
      else if(me.is(Zerg.Mutalisk))
        1.25
      else
        1.0
    Math.min(min, max)
  }
}
