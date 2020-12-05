package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.BattleLocal
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Mathematics.Shapes.Spiral
import Micro.Actions.Scouting.BlockConstruction
import Micro.Heuristics.MicroValue
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
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
  lazy val allUnits               : Vector[UnitInfo]    = battle.map(b => b.teams.flatMap(_.units)    ++ With.matchups.entrants.getOrElse(b, Seq.empty)                                ).getOrElse(defaultUnits).distinct
  lazy val enemies                : Vector[UnitInfo]    = battle.map(b => b.teamOf(me).opponent.units ++ With.matchups.entrants.getOrElse(b, Seq.empty).view.filter(   _.isEnemyOf(me))).getOrElse(defaultUnits).distinct
  lazy val alliesInclSelf         : Vector[UnitInfo]    = battle.map(b => b.teamOf(me).units          ++ With.matchups.entrants.getOrElse(b, Seq.empty).view.filter( ! _.isEnemyOf(me))).getOrElse(Vector.empty).distinct
  lazy val alliesInclSelfCloaked  : Vector[UnitInfo]    = alliesInclSelf.filter(_.cloakedOrBurrowed)
  lazy val allies                 : Vector[UnitInfo]    = alliesInclSelf.filterNot(_.id == me.id)
  lazy val others                 : Vector[UnitInfo]    = enemies ++ allies
  lazy val allyDetectors          : Vector[UnitInfo]    = allies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val enemyDetectors         : Vector[UnitInfo]    = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  lazy val threats                : Vector[UnitInfo]    = enemies.filter(threatens(_, me))
  lazy val targets                : Vector[UnitInfo]    = enemies.filter(threatens(me, _))
  lazy val threatsInRange         : Vector[UnitInfo]    = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - threat.pixelsTravelledMax(frame))
  lazy val targetsInRange         : Vector[UnitInfo]    = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me, at) - me.pixelsTravelledMax(frame) - target.pixelsTravelledMax(frame) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val nearestArbiter         : Option[UnitInfo]    = ByOption.minBy(allies.view.filter(_.is(Protoss.Arbiter)))(_.pixelDistanceSquared(me))
  lazy val allyTemplarCount       : Int                 = allies.count(_.is(Protoss.HighTemplar))

  def canCatchMe(catcher: UnitInfo): Boolean = {
    if (catcher.unitClass.isWorker) return false
    lazy val catcherSpeed = Math.max(catcher.topSpeed, catcher.friendly.flatMap(_.transport.map(_.topSpeed)).getOrElse(0.0))
    lazy val catcherFlying = catcher.flying || catcher.friendly.exists(_.agent.ride.exists(_.flying)) && ! me.flying
    val output = (
      catcherSpeed * (if (catcherFlying) 2 else 1) >= me.topSpeed
        // The Math.max prevents Zealots from thinking they can catch SCVs
        || (catcher.pixelRangeAgainst(me) > Math.max(32 * 2, me.effectiveRangePixels) && catcher.friendly.exists(_.squadenemies.contains(me)))
        || busyForCatching
        || catcher.is(Zerg.Scourge)
        || catcher.framesToGetInRange(me) < 8
        || (me.unitClass.isWorker && me.base.exists(_.harvestingArea.contains(me.tileIncludingCenter)))
        || (catcher.is(Zerg.Zergling) && With.self.hasUpgrade(Zerg.ZerglingSpeed) && ! me.player.hasUpgrade(Terran.VultureSpeed))
        || (catcher.is(Protoss.Zealot) && me.isDragoon() && ! me.player.hasUpgrade(Protoss.DragoonRange))
        || (catcher.is(Protoss.DarkTemplar) && me.isDragoon()))
    output
  }
  lazy val busyForCatching  : Boolean           = me.gathering || me.constructing || me.repairing || ! me.canMove || BlockConstruction.buildOrders.contains(me.order)
  lazy val catchers         : Vector[UnitInfo]  = threats.filter(canCatchMe)
  
  private def threatens(shooter: UnitInfo, victim: UnitInfo): Boolean = {
    if (shooter.canAttack(victim)) return true
    if (shooter.unitClass.canAttack(victim)
      && victim.detected
      && shooter.framesToBeReadyForAttackOrder < victim.framesToTravelPixels(shooter.pixelRangeAgainst(victim) - shooter.pixelDistanceEdge(victim))) return true
    false
  }
  def repairers: ArrayBuffer[UnitInfo] = ArrayBuffer.empty ++ allies.filter(_.friendly.exists(_.agent.toRepair.contains(me)))

  lazy val firstTeammate                  : UnitInfo              = ByOption.minBy(alliesInclSelf)(_.id).getOrElse(me)
  lazy val valuePerDamage                 : Double                = MicroValue.valuePerDamageCurrentHp(me)
  lazy val dpfDealingMax                  : Double                = splashFactorMax * ByOption.max(targets.view.map(me.dpfOnNextHitAgainst)).getOrElse(0.0)
  lazy val vpfDealingMax                  : Double                = splashFactorMax * ByOption.max(targets.view.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val vpfDealingInRange              : Double                = splashFactorInRange * ByOption.max(targetsInRange.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val dpfReceiving                   : Double                = threatsInRange.view.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val vpfReceiving                   : Double                = valuePerDamage * dpfReceiving
  lazy val vpfNet                         : Double                = vpfDealingInRange - vpfReceiving
  lazy val vpfTargetHeuristic             : Double                = TargetHeuristicVpfEnemy.calculate(me)
  lazy val framesBeforeAttacking          : Double                = ByOption.max(targets.view.map(me.framesBeforeAttacking)).getOrElse(Forever()).toDouble
  lazy val framesToLive                   : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceiving)
  lazy val doomed                         : Boolean               = framesToLive <= framesOfEntanglement
  lazy val pixelsOfEntanglementPerThreat  : Map[UnitInfo, Double] = threats.view.map(threat => (threat, pixelsOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglementPerThreat  : Map[UnitInfo, Double] = threats.view.map(threat => (threat, framesOfEntanglementWith(threat))).toMap
  lazy val framesOfEntanglement           : Double                = ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever())
  lazy val pixelsOfEntanglement           : Double                = ByOption.max(pixelsOfEntanglementPerThreat.values).getOrElse(- With.mapPixelWidth)
  lazy val framesOfSafety                 : Double                = - With.latency.latencyFrames - With.reaction.agencyAverage - ByOption.max(framesOfEntanglementPerThreat.values).getOrElse(- Forever().toDouble)
  lazy val pixelsOutOfNonWorkerRange      : Double                = ByOption.min(threats.view.filterNot(_.unitClass.isWorker).map(t => t.pixelDistanceEdge(me) - t.pixelRangeAgainst(me))).getOrElse(With.mapPixelWidth)
  lazy val teamFramesOfSafety             : Double                = if (me == firstTeammate) ByOption.min(alliesInclSelf.view.map(_.matchups.framesOfSafety)).getOrElse(0) else firstTeammate.matchups.teamFramesOfSafety
  lazy val tilesOfInvisibility            : Double                = if (me.visibleToOpponents) 0 else Spiral.points(8).map(me.tileIncludingCenter.add).find(With.grids.enemyVision.isSet).map(_.tileDistanceFast(me.tileIncludingCenter)).getOrElse(maxTilesOfInvisibility)
  val maxTilesOfInvisibility = 8

  def dpfDealingDiffused(target: UnitInfo): Double = splashFactorInRange * me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)

  def pixelsOfEntanglementWith(threat: UnitInfo, fixedRange: Option[Double] = None): Double = {
    val speedTowardsThreat    = me.speedApproaching(threat)
    val framesToStopMe        = if(speedTowardsThreat <= 0) 0.0 else me.framesToStopRightNow
    val framesToFlee          = framesToStopMe + me.unitClass.framesToTurn180 + With.latency.framesRemaining + With.reaction.agencyAverage
    val distanceClosedByMe    = speedTowardsThreat * framesToFlee
    val distanceClosedByEnemy = if (threat.is(Protoss.Interceptor)) 0.0 else (threat.topSpeed * framesToFlee)
    val distanceEntangled     = threat.pixelRangeAgainst(me) - threat.pixelDistanceEdge(me)
    val output                = distanceEntangled + distanceClosedByEnemy
    output
  }

  def framesOfEntanglementWith(threat: UnitInfo): Double = {
    /*
    This math stinks. I'm looking at a High Templar that's 407 pixels (12.7 tiles) away from a Marine,
    with 8 avg agency frames, 3 latency frames,
    and the entanglement is -6.8 while the safety is -4.2 frames
    That is, we're saying the templar has 4 frames to turn and run before the marine will dash 7.7 tiles and shoot it.
    Something's really busted here.
     */
    lazy val approachSpeedMe      = me.speedApproaching(threat.pixelCenter)
    lazy val approachSpeedThreat  = if (threat.is(Protoss.Interceptor)) 0.0 else threat.speedApproaching(me.pixelCenter)
    lazy val approachSpeedTotal   = approachSpeedMe + approachSpeedThreat
    lazy val framesToTurn         = me.unitClass.framesToTurn180 // Should be this, but for performance limitations: me.unitClass.framesToTurn(me.angleRadians - threat.pixelCenter.radiansTo(me.pixelCenter))
    lazy val framesToAccelerate   = (me.topSpeed + approachSpeedMe + approachSpeedThreat) / me.unitClass.accelerationFrames
    lazy val blastoffFrames       = if (me.unitClass.canMove) framesToTurn + framesToAccelerate else 0 //How long for us to turn around and run
    lazy val reactionFrames       = With.reaction.agencyMax + 2 * With.latency.framesRemaining
    lazy val threatRangeBonus     = if (threat.isFriendly) 0.0 else Math.max(0.0, approachSpeedTotal * reactionFrames)
    
    val effectiveRange = threat.pixelRangeAgainst(me) + threatRangeBonus
    val gapPixels = me.pixelDistanceEdge(threat) - effectiveRange
    
    val gapSpeed          = if (gapPixels >= 0 && threat.canMove) threat.topSpeed else me.topSpeed
    val framesToCloseGap  = PurpleMath.nanToInfinity(Math.abs(gapPixels) / gapSpeed)
    val output            = framesToCloseGap * PurpleMath.signum( - gapPixels) + blastoffFrames
    
    output
  }
  
  lazy val splashFactorMax: Double = splashFactorForUnits(targets)
  lazy val splashFactorInRange: Double = splashFactorForUnits(targetsInRange)
  
  protected def splashFactorForUnits(targetsConsidered: Iterable[UnitInfo]): Double = PurpleMath.clamp(me.unitClass.splashFactor, 1.0, targetsConsidered.size)

  object TargetHeuristicVpfEnemy {

    // Divisor to make vpfs scale on 1.0+
    // Based on the lowest VPF in the game: Arbiter against Zergling :)
    lazy val baselineVpf: Double = Vector(
      Zerg.Zergling.subjectiveValue * 0.5 * Protoss.Arbiter.effectiveGroundDamage / Protoss.Arbiter.groundDamageCooldown,
      With.economy.incomePerFrameMinerals,
      With.economy.incomePerFrameGas * MicroValue.gasToMineralsRatio).min

    private val incompleteCompletionHorizon = 24 * 5
    protected def incompleteBuildingVpf(unit: UnitInfo): Double = {
      val dpfAir    = PurpleMath.nanToZero(unit.unitClass.effectiveAirDamage / unit.unitClass.airDamageCooldown)
      val dpfGround = PurpleMath.nanToZero(unit.unitClass.effectiveGroundDamage / unit.unitClass.groundDamageCooldown)
      val vpfAir    = dpfAir    * ByOption.max(unit.matchups.enemies.filter(   _.flying).map(_.subjectiveValue)).getOrElse(0.0)
      val vpfGround = dpfGround * ByOption.max(unit.matchups.enemies.filter( ! _.flying).map(_.subjectiveValue)).getOrElse(0.0)
      val discount  = Math.max(0.0, (incompleteCompletionHorizon - unit.remainingCompletionFrames) / incompleteCompletionHorizon)
      val output    = Math.max(vpfAir, vpfGround) * discount
      output
    }

    def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
      lazy val maxTeamVpf = ByOption.max(unit.matchups.enemies.view.filter(_ != candidate).map(calculate)).getOrElse(0.0)
      val baseVpf = candidate.matchups.vpfTargetHeuristic

      var bonusVpf = 0.0

      // Bunker builders are super high priority
      if (candidate.constructing && candidate.orderTarget.exists(_.is(Terran.Bunker))) {
        bonusVpf += 2.0 * maxTeamVpf
      }
      // Turret builders too, if detection is important to them
      if (candidate.constructing && candidate.orderTarget.exists(_.is(Terran.MissileTurret)) && unit.matchups.alliesInclSelfCloaked.exists(_.canAttack)) {
        bonusVpf += maxTeamVpf
      }
      if (candidate.repairing) {
        candidate.orderTarget.foreach(bonusVpf += calculate(_))
      }
      if (candidate.is(Protoss.Arbiter) && unit.matchups.allyDetectors.isEmpty) {
        bonusVpf += maxTeamVpf
      }
      if (With.blackboard.pushKiters.get && candidate.isAny(Terran.Vulture, Protoss.Dragoon)) {
        bonusVpf += baseVpf
      }

      baseVpf + bonusVpf
    }

    def calculate(candidate: UnitInfo): Double = {
      val numerator =
        if (candidate.gathering || candidate.base.exists(_.harvestingArea.contains(candidate.tileIncludingCenter))) {
          With.economy.incomePerFrameMinerals
        }
        else if (candidate.constructing && candidate.target.isDefined) {
          Math.max(
            incompleteBuildingVpf(candidate.target.get),
            candidate.target.get.subjectiveValue / candidate.target.get.unitClass.buildFrames)
        }
        else if (candidate.unitClass.isBuilding && ! candidate.complete) {
          incompleteBuildingVpf(candidate)
        }
        else {
          val vpfNow = candidate.matchups.vpfDealingInRange
          val vpfMax = candidate.matchups.vpfDealingMax
          vpfNow + 0.1 * vpfMax
        }

      val output = numerator / baselineVpf
      output
    }
  }
}
