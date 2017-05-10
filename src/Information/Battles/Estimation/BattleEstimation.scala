package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEstimation(
  val tacticsUs         : TacticsOptions,
  val tacticsEnemy      : TacticsOptions,
  val battle            : Option[Battle],
  val considerGeometry  : Boolean) {
  
  ///////////
  // Setup //
  ///////////
  
  private val unitsOurs   = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  private val unitsEnemy  = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  private val avatarUs    = new BattleEstimationUnit
  private val avatarEnemy = new BattleEstimationUnit
  
  def addUnits(battle: Battle) {
    battle.us.units.foreach(addUnit)
    battle.enemy.units.foreach(addUnit)
  }
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly) {
      unitsOurs.put(unit, new BattleEstimationUnit(unit, tacticsUs, battle, battle.map(_.us), considerGeometry))
      avatarUs.add(unitsOurs(unit))
    }
    else {
      unitsEnemy.put(unit, new BattleEstimationUnit(unit, tacticsEnemy, battle, battle.map(_.enemy), considerGeometry))
      avatarEnemy.add(unitsEnemy(unit))
    }
  }
  
  def removeUnit(unit: UnitInfo) {
    unitsOurs.get(unit).foreach(unitProxy => {
      avatarUs.remove(unitsOurs(unit))
      unitsOurs.remove(unit)
    })
    unitsEnemy.get(unit).foreach(unitProxy => {
      avatarEnemy.remove(unitsEnemy(unit))
      unitsEnemy.remove(unit)
    })
  }
  
  def updateUnit(unit: UnitInfo) {
    removeUnit(unit)
    addUnit(unit)
  }
  
  def eligible(unit:UnitInfo):Boolean = {
    if (unit.unitClass.isBuilding && ! unit.unitClass.helpsInCombat) return false
    if (unit.is(Protoss.Scarab))      return false
    if (unit.is(Protoss.Interceptor)) return false
    true
  }
  
  /////////////////
  // Calculation //
  /////////////////
  
  var result = new BattleEstimationResult
  
  def recalculate() {
    result = new BattleEstimationResult
    
    if (avatarUs.totalUnits == 0 || avatarEnemy.totalUnits == 0) return
    
    // Parameters
    //
    // frameStep helps us account for dropoff in damage as units die.
    // Two levers affect how this works:
    // * how we calculate # of living units. The current calculation assumes that all damage is focus fired (an overestimate of dropoff)
    // * frameStep, which when larger reduces the impact of dropoff. Let's choose a big one (to balance the above with an underestimate of dropoff)
    //
    val frameStep = 24
    val framesMax = frameStep * 12
    
    // Initial state
    //
    val meanDistanceUs    = avatarUs.pixelsFromEnemy     / avatarUs.totalUnits
    val meanDistanceEnemy = avatarEnemy.pixelsFromEnemy   / avatarEnemy.totalUnits
    val xUs               = -meanDistanceUs
    val xEnemy            = meanDistanceEnemy
    var stateUs           = BattleEstimationState(avatarUs,    tacticsUs,    xUs,     meanDistanceUs)
    var stateEnemy        = BattleEstimationState(avatarEnemy, tacticsEnemy, xEnemy,  meanDistanceEnemy)
  
    updateParticipation(stateUs,     stateEnemy)
    updateParticipation(stateEnemy,  stateUs)
    if (With.configuration.visualizeBattles) {
      result.statesUs     += stateUs
      result.statesEnemy  += stateEnemy
    }
  
    // Run the estimation!
    //
    (0 to framesMax by frameStep).foreach(frame => {
      if (
        stateUs.damageReceived    < avatarUs.totalHealth &&
        stateEnemy.damageReceived < avatarEnemy.totalHealth) {
        
        result.frames = frame
        
        // 1. Move
        //
        var nextStateUs     = move(frameStep, stateUs,     stateEnemy)
        var nextStateEnemy  = move(frameStep, stateEnemy,  stateUs)
        stateUs     = nextStateUs
        stateEnemy  = nextStateEnemy
        updateParticipation(stateUs,     stateEnemy)
        updateParticipation(stateEnemy,  stateUs)
        
        // 2. Deal damage
        //
        nextStateUs     = dealDamage(frameStep, stateEnemy, stateUs)
        nextStateEnemy  = dealDamage(frameStep, stateUs,    stateEnemy)
        stateUs     = nextStateUs
        stateEnemy  = nextStateEnemy
        
        // 3. Log
        //
        if (With.configuration.visualizeBattles) {
          result.statesUs     += stateUs
          result.statesEnemy  += stateEnemy
        }
      }
    })
    
    result.costToUs     = totalCost(framesMax, stateUs)
    result.costToEnemy  = totalCost(framesMax, stateEnemy)
  }
  
  private def move(
    frameStep: Int,
    stateThis: BattleEstimationState,
    stateThat: BattleEstimationState)
      : BattleEstimationState = {
    
    val output = stateThis.copy()
    
    val direction = signTowards(stateThis.x, stateThat.x)
    
    val xTarget =
      if      (stateThis.tactics.has(Tactics.Movement.Charge))  stateThat.x
      else if (stateThis.tactics.has(Tactics.Movement.Flee))    - direction * 1000.0
      else                                                      stateThat.x - direction * Math.max(stateThis.avatar.rangePixelsGround, stateThis.avatar.rangePixelsAir)
    
    val regroupRate =
      if      (stateThis.tactics.has(Tactics.Movement.Charge))  0.0
      else if (stateThis.tactics.has(Tactics.Movement.Flee))    2.0
      else                                                      1.0
    
    val speedPixelsPerFrame  = stateThis.avatar.speedPixelsPerFrame / stateThis.avatar.totalUnits
    val distanceToOpponent   = Math.abs(xTarget - stateThis.x)
    val distanceTravelled    = Math.min(distanceToOpponent, speedPixelsPerFrame * frameStep)
    
    output.x                += direction * distanceTravelled
    output.pixelsRegrouped  += regroupRate * speedPixelsPerFrame
    output
  }
  
  private def signTowards(xFrom:Double, xTo:Double):Double = {
    val sign = Math.signum(xTo - xFrom)
    if (Math.abs(sign) <= 1.0) 1.0 else sign
  }
  
  private def updateParticipation(
    stateThis: BattleEstimationState,
    stateThat: BattleEstimationState) {
    
    stateThis.deaths                 = deaths(stateThis.avatar, stateThis.damageReceived)
    stateThis.arrivedGroundShooters += stateThis.pixelsRegrouped + stateThis.avatar.rangePixelsGround / stateThis.pixelsAway
    stateThis.arrivedAirShooters    += stateThis.pixelsRegrouped + stateThis.avatar.rangePixelsAir    / stateThis.pixelsAway
    stateThis.arrivedGroundShooters  = Math.min(stateThis.avatar.totalUnits, stateThis.arrivedGroundShooters)
    stateThis.arrivedAirShooters     = Math.min(stateThis.avatar.totalUnits, stateThis.arrivedAirShooters)
  }
  
  private def dealDamage(
    frameStep : Int,
    fromState : BattleEstimationState,
    toState   : BattleEstimationState)
      : BattleEstimationState = {
    
    val output = toState.copy()
    
    val from    = fromState.avatar
    val to      = toState.avatar
    val tactics = fromState.tactics
    
    val airFocus    = if (tactics.has(Tactics.Focus.Ground)) 0.0 else if (tactics.has(Tactics.Focus.Air)) 1.0 else to.totalFlyers / to.totalUnits
    val groundFocus = 1.0 - airFocus
  
    val unitsShootingAliveRatio = 1.0 - fromState.deaths / fromState.avatar.totalUnits
    val unitsShootingGround     = unitsShootingAliveRatio * fromState.arrivedGroundShooters
    val unitsShootingAir        = unitsShootingAliveRatio * fromState.arrivedAirShooters
    val toDenominator           = toState.avatar.totalUnits
    val fromDenominator         = fromState.avatar.totalUnits
    
    val damagePerFramePerUnit =
      to.vulnerabilityGroundConcussive  / toDenominator * unitsShootingGround / fromDenominator * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
      to.vulnerabilityGroundExplosive   / toDenominator * unitsShootingGround / fromDenominator * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
      to.vulnerabilityGroundNormal      / toDenominator * unitsShootingGround / fromDenominator * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
      to.vulnerabilityAirConcussive     / toDenominator * unitsShootingAir    / fromDenominator * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
      to.vulnerabilityAirExplosive      / toDenominator * unitsShootingAir    / fromDenominator * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
      to.vulnerabilityAirNormal         / toDenominator * unitsShootingAir    / fromDenominator * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    output.damageReceived += damagePerFramePerUnit * frameStep / to.totalUnits
    output.damageReceived = Math.min(output.damageReceived, toState.avatar.totalHealth)
    output
  }
  
  
  // Examples:
  // 1 unit,   99 damage,  100 hp = 0 deaths
  // 2 units,  199 damage, 200 hp = 1 death
  // 2 units,  99 damage,  200 hp = 0 deaths
  //
  private def deaths(avatar:BattleEstimationUnit, damage:Double):Double = {
    Math.min(avatar.totalUnits, Math.floor(avatar.totalUnits * damage / avatar.totalHealth))
  }
  
  private def totalCost(frames: Int, state:BattleEstimationState) = {
    state.avatar.subjectiveValueCostPerFrame * frames +
      state.avatar.subjectiveValue * state.damageReceived / state.avatar.totalHealth
  }
}
