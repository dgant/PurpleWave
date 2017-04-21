package Information.Battles.Estimation

import Information.Battles.BattleTypes.{Battle, BattleGroup}
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions, TacticsOptionsDefault}
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.UnitSizeType

import scala.collection.mutable

object BattleEstimator {
  
  def run() {
    With.battles.local.filter(_.happening).foreach(estimate)
  }
  
  def estimate(battle:Battle) {
    battle.estimations = battle.us.tacticsAvailable.map(ourTactics => estimateWithTactics(battle, ourTactics, new TacticsOptionsDefault))
  }
  
  def estimateWithTactics(battle:Battle, tacticsUs:TacticsOptions, tacticsEnemy:TacticsOptions):BattleEstimation = {
  
    val fightDuration = 24 * (
      (if (tacticsUs    .has(Tactics.Movement.Charge))  4 else 0) +
      (if (tacticsEnemy .has(Tactics.Movement.Charge))  4 else 0) +
      (if (tacticsUs    .has(Tactics.Movement.Kite))    2 else 0) +
      (if (tacticsEnemy .has(Tactics.Movement.Kite))    2 else 0))
    
    var damageDealtToUs          = 0.0
    var damageDealtToEnemy       = 0.0
    
    val participantsUs      = buildParticipants(battle, battle.us, battle.enemy, tacticsUs, tacticsEnemy, fightDuration)
    val participantsEnemy   = buildParticipants(battle, battle.enemy, battle.us, tacticsEnemy, tacticsUs, fightDuration)
    
    if (participantsUs.isEmpty || participantsEnemy.isEmpty) {
      return new BattleEstimation(battle, tacticsUs, 0, 0, participantsUs, participantsEnemy, newDamageMap, newDamageMap)
    }
    
    val damageSpreadUs      = participantsUs.size
    val damageSpreadEnemy   = participantsEnemy.size
    
    val damageDealtByUs     = newDamageMap
    val damageDealtByEnemy  = newDamageMap
    
    participantsUs    .foreach(dealDamage(_, damageDealtByUs))
    participantsEnemy .foreach(dealDamage(_, damageDealtByEnemy))
    
    damageDealtToUs    = participantsUs    .map(costOfDamage(_, damageDealtByEnemy)).sum / participantsUs.size
    damageDealtToEnemy = participantsEnemy .map(costOfDamage(_, damageDealtByEnemy)).sum / participantsEnemy.size
      
    new BattleEstimation(
      battle,
      tacticsUs,
      damageDealtToUs,
      damageDealtToEnemy,
      participantsUs,
      participantsEnemy,
      damageDealtByUs,
      damageDealtByEnemy)
  }
  
  type DamageMap = mutable.HashMap[VulnerabilityProfile, Double]
  
  private val small = 0
  private val medium = 1
  private val large = 2
  
  private def newDamageMap:DamageMap = {
    val output = new mutable.HashMap[VulnerabilityProfile, Double]
    output(VulnerabilityProfile(false,  small))  = 0
    output(VulnerabilityProfile(false,  medium)) = 0
    output(VulnerabilityProfile(false,  large))  = 0
    output(VulnerabilityProfile(true,   small))  = 0
    output(VulnerabilityProfile(true,   medium)) = 0
    output(VulnerabilityProfile(true,   large))  = 0
    output
  }
  
  private def dealDamage(attacker:Participant, damageMap:DamageMap) {
    damageMap(new VulnerabilityProfile(false, small))    = attacker.involvement * attacker.dpsGroundSmall
    damageMap(new VulnerabilityProfile(false, medium))   = attacker.involvement * attacker.dpsGroundMedium
    damageMap(new VulnerabilityProfile(false, large))    = attacker.involvement * attacker.dpsGroundLarge
    damageMap(new VulnerabilityProfile(true,  small))    = attacker.involvement * attacker.dpsAirSmall
    damageMap(new VulnerabilityProfile(true,  medium))   = attacker.involvement * attacker.dpsAirMedium
    damageMap(new VulnerabilityProfile(true,  large))    = attacker.involvement * attacker.dpsAirLarge
  }
  
  private def costOfDamage(defender:Participant, damageMap:DamageMap):Double = {
    damageMap(defender.vulnerabilityProfile) * defender.exposure * defender.valueRatio
  }
  
  private case class VulnerabilityProfile(flying: Boolean, size: Int)
  
  class Participant(
    val dpsGroundSmall        : Double,
    val dpsGroundMedium       : Double,
    val dpsGroundLarge        : Double,
    val dpsAirSmall           : Double,
    val dpsAirMedium          : Double,
    val dpsAirLarge           : Double,
    val involvement           : Double,
    val exposure              : Double,
    val vulnerabilityProfile  : VulnerabilityProfile,
    val valueRatio            : Double)
  
  private def buildParticipants(
    battle        : Battle,
    groupThis     : BattleGroup,
    groupThat     : BattleGroup,
    tacticsThis   : TacticsOptions,
    tacticsThat   : TacticsOptions,
    fightDuration : Int)
      : Vector[Participant] = {
    
    val enemyRangeMean    = Math.max(32.0, PurpleMath.mean(groupThat.units.filter(_.canAttackThisSecond).map(_.pixelRangeMax)))
    val flyerValue        = groupThat.units.filter(_.flying).map(_.subjectiveValue).sum.toDouble
    val groundValue       = groupThat.units.filter(! _.flying).map(_.subjectiveValue).sum.toDouble
    val enemyGroundRatio  = if (groundValue + flyerValue == 0) 1.0 else groundValue / (flyerValue + groundValue)
    
    groupThis.units
      //Irrelevant units are only participating if their team is abandoning them
      .filter(unit => unit.alive && (tacticsThis.has(Tactics.Movement.Flee) || unit.unitClass.helpsInCombat))
      .map(unit => buildParticipant(
        battle,
        unit,
        enemyRangeMean,
        enemyGroundRatio,
        tacticsThis,
        tacticsThat,
        fightDuration))
  }
  
  private def buildParticipant(
    battle            : Battle,
    unit              : UnitInfo,
    enemyRangeMean    : Double,
    enemyGroundRatio  : Double,
    tacticsThis       : TacticsOptions,
    tacticsThat       : TacticsOptions,
    fightDuration     : Int)
      : Participant = {
    
    val canAttack                   = unit.canAttackThisSecond
    val attacksGround               = unit.unitClass.attacksGround
    val attacksAir                  = unit.unitClass.attacksAir
    val damageTypeGround            = unit.unitClass.groundDamageTypeRaw
    val damageTypeAir               = unit.unitClass.airDamageTypeRaw
    val dpsGround                   = unit.groundDps
    val dpsAir                      = unit.airDps
    val vulnerabilityProfile        = new VulnerabilityProfile(unit.flying, sizeTypeToInt(unit.unitClass.size))
    val value                       = unit.unitClass.mineralValue * 3.0 + unit.unitClass.gasValue * 2.0
    val valueRatio                  = value * 2 / (unit.totalHealth + unit.unitClass.maxTotalHealth)
    
    val distanceFactor              = 32.0 * 4.0 //How far from the focus renders a unit irrelevant
    val isFighting                  = isFighter(unit, tacticsThis)
    val isCharging                  = isFighting && tacticsThis.has(Tactics.Movement.Charge)
    val isFleeing                   = isFleer(unit, tacticsThis)
    val currentDistanceFromFocus    = unit.pixelDistanceFast(battle.focus)
    val targetDistanceFromFocus     = if (isFighting) unit.pixelRangeMax else Terran.SiegeTankSieged.maxAirGroundRange
    
    val involvementDistanceInitial  = Math.max(0.0, currentDistanceFromFocus - unit.pixelRangeMax)
    val involvementDistanceFinal    = Math.max(targetDistanceFromFocus, involvementDistanceInitial - unit.topSpeed * fightDuration)
    val involvementCap              = if (isCharging) 1.0 else if (isFighting) 0.75 else 0.0
    val involvement                 = Math.min(involvementCap, Math.max(0.0, 1.0 - (involvementDistanceInitial + involvementDistanceFinal) / 2.0 / distanceFactor))
    
    val exposureDistanceInitial     = Math.max(0.0, currentDistanceFromFocus - enemyRangeMean)
    val exposureDistanceFinal       = if (isFleeing) exposureDistanceInitial + unit.topSpeed * fightDuration else involvementDistanceFinal
    val exposureCap                 = Math.max(involvementCap, 0.5)
    val exposure                    = Math.min(exposureCap, Math.max(0.0, 1.0 - (exposureDistanceInitial + exposureDistanceFinal) / 2.0 / distanceFactor)  )

    val attackGround      = unit.groundDps  > 0 && ! tacticsThis.has(Tactics.Focus.Air)
    val attackAir         = unit.airDps     > 0 && ! tacticsThis.has(Tactics.Focus.Ground)
    val damageFocusGround = if (attackGround && attackAir)       enemyGroundRatio else if (attackGround)  1.0 else 0.0
    val damageFocusAir    = if (attackGround && attackAir) 1.0 - enemyGroundRatio else if (attackAir)     1.0 else 0.0
    val dpsGroundSmall    = if (isFighting) damageFocusGround * unit.groundDps  * Damage.scaleBySize(unit.unitClass.groundDamageTypeRaw,  UnitSizeType.Small)   else 0.0
    val dpsGroundMedium   = if (isFighting) damageFocusGround * unit.groundDps  * Damage.scaleBySize(unit.unitClass.groundDamageTypeRaw,  UnitSizeType.Medium)  else 0.0
    val dpsGroundLarge    = if (isFighting) damageFocusGround * unit.groundDps  * Damage.scaleBySize(unit.unitClass.groundDamageTypeRaw,  UnitSizeType.Large)   else 0.0
    val dpsAirSmall       = if (isFighting) damageFocusAir    * unit.airDps     * Damage.scaleBySize(unit.unitClass.airDamageTypeRaw,     UnitSizeType.Small)   else 0.0
    val dpsAirMedium      = if (isFighting) damageFocusAir    * unit.airDps     * Damage.scaleBySize(unit.unitClass.airDamageTypeRaw,     UnitSizeType.Medium)  else 0.0
    val dpsAirLarge       = if (isFighting) damageFocusAir    * unit.airDps     * Damage.scaleBySize(unit.unitClass.airDamageTypeRaw,     UnitSizeType.Large)   else 0.0
    
    new Participant(
      dpsGroundSmall,
      dpsGroundMedium,
      dpsGroundLarge,
      dpsAirSmall,
      dpsAirMedium,
      dpsAirLarge,
      involvement,
      exposure,
      vulnerabilityProfile,
      valueRatio)
  }
  
  private def sizeTypeToInt(sizeType:UnitSizeType):Int =
    sizeType match {
      case UnitSizeType.Small => small
      case UnitSizeType.Large => large
      case _                  => medium
    }
  
  private def meanSpeed(units:Traversable[UnitInfo]):Double = {
    PurpleMath.mean(units.filter(unit => unit.alive && unit.canMoveThisFrame).map(_.topSpeed))
  }
  
  //Hacky way to implement the "fight with half of all workers" tactic
  //And yes, this is static statefulness (which we try to avoid) but of a hopefully innocuous kind.
  private var acceptNextWorker:Boolean = false
  
  private def isFighter(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    if ( ! unit.alive) return false
    if (unit.unitClass.isWorker) {
      if (tactics.has(Tactics.Workers.FightAll)) {
        return true
      }
      if (tactics.has(Tactics.Workers.FightHalf)) {
        acceptNextWorker = ! acceptNextWorker
        return acceptNextWorker
      }
      return false
    }
    if (unit.wounded && tactics.has(Tactics.Wounded.Flee)) {
      return false
    }
    if (tactics.has(Tactics.Movement.Flee)) {
      return false
    }
    unit.unitClass.helpsInCombat
  }
  
  private def isFleer(unit:UnitInfo, tactics:TacticsOptions):Boolean = {
    unit.canMoveThisFrame &&
      ! isFighter(unit, tactics) &&
      ( ! unit.unitClass.isWorker || tactics.has(Tactics.Workers.Flee))
  }
}
