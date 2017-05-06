package Information.Battles.Estimation

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.PurpleMath
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
    
    val frameStep   = 24
    val frames      = frameStep * 12
    var stateUs     = BattleEstimationState(avatarUs,    tacticsUs,    -avatarUs.pixelsFromFocus    / avatarUs.totalUnits)
    var stateEnemy  = BattleEstimationState(avatarEnemy, tacticsEnemy, avatarEnemy.pixelsFromFocus  / avatarEnemy.totalUnits)
    
    // Account for dropoff in damage as units die
    // Two levers affect how this works:
    // * how we calculate # of living units. The current calculation assumes that all damage is focus fired (an overestimate of dropoff)
    // * frameStep, which when larger reduces the impact of dropoff. Let's choose a big one (to balance the above with an underestimate of dropoff)
    (0 to frames by frameStep).foreach(frame => {
      if (stateUs.damage < avatarUs.totalHealth && stateEnemy.damage < avatarEnemy.totalHealth) {
        
        result.frames = frame
        
        var nextStateUs     = move(frameStep, stateUs,     stateEnemy)
        var nextStateEnemy  = move(frameStep, stateEnemy,  stateUs)
        stateUs     = nextStateUs
        stateEnemy  = nextStateEnemy
        
        updateParticipation(stateUs,     stateEnemy)
        updateParticipation(stateEnemy,  stateUs)
        
        nextStateUs     = dealDamage(frameStep, stateEnemy, stateUs)
        nextStateEnemy  = dealDamage(frameStep, stateUs,    stateEnemy)
        stateUs     = nextStateUs
        stateEnemy  = nextStateEnemy
        
        if (With.configuration.visualizeBattles) {
          result.statesUs     += stateUs
          result.statesEnemy  += stateEnemy
        }
      }
    })
    
    result.costToUs     = totalCost(frames, stateUs)
    result.costToEnemy  = totalCost(frames, stateEnemy)
  }
  
  private def move(
    frameStep: Int,
    stateThis: BattleEstimationState,
    stateThat: BattleEstimationState)
      : BattleEstimationState = {
    
    val output = stateThis.copy()
    
    val xTarget =
      if      (stateThis.tactics.has(Tactics.Movement.Charge))  stateThat.x
      else if (stateThis.tactics.has(Tactics.Movement.Flee))    - signTowards(stateThis.x, stateThat.x) * 100.0
      else                                                      stateThis.x
    
    val speedPixelsPerFrame = stateThis.avatar.speedPixelsPerFrame / stateThis.avatar.totalUnits
    val distanceTravelled = Math.min(speedPixelsPerFrame * frameStep, Math.abs(xTarget - stateThis.x))
    output.x += signTowards(stateThis.x, stateThat.x) * distanceTravelled
    
    val distanceRegrouped = Math.max(0.0, Math.min(stateThis.spread, 2 * (speedPixelsPerFrame - distanceTravelled)))
    output.spread -= distanceRegrouped
    
    output
  }
  
  private def signTowards(xFrom:Double, xTo:Double):Double = {
    val sign = Math.signum(xTo - xFrom)
    if (Math.abs(sign) <= 1.0) 1.0 else sign
  }
  
  private def updateParticipation(
    stateThis: BattleEstimationState,
    stateThat: BattleEstimationState) {
    
    val distance                  = Math.abs(stateThis.x - stateThat.x)
    val expectedSpread            = Math.sqrt(stateThis.avatar.totalUnits * 32.0)
    val spreadFactor              = PurpleMath.clampToOne(expectedSpread / stateThis.spread)
    stateThis.participationGround = spreadFactor * PurpleMath.nanToOne(PurpleMath.clampToOne(stateThis.avatar.rangePixelsGround / (distance - stateThis.spread)))
    stateThis.participationAir    = spreadFactor * PurpleMath.nanToOne(PurpleMath.clampToOne(stateThis.avatar.rangePixelsAir    / (distance - stateThis.spread)))
  }
  
  private def clampToOne(value:Double):Double = Math.max(0.0, Math.min(1.0, value))
  
  private def dealDamage(
    frameStep : Int,
    fromState : BattleEstimationState,
    toState   : BattleEstimationState)
      : BattleEstimationState = {
    
    val output = toState.copy()
    
    val from        = fromState.avatar
    val to          = toState.avatar
    val fromTactics = fromState.tactics
    val ratioAlive  = livingUnitsRatio(fromState.avatar, fromState.damage)
    val airFocus    = if (fromTactics.has(Tactics.Focus.Ground)) 0.0 else if (fromTactics.has(Tactics.Focus.Air)) 1.0 else to.totalFlyers / to.totalUnits
    val groundFocus = 1.0 - airFocus
    
    val damagePerFrameTimesUnits =
      to.damageScaleGroundConcussive  * fromState.participationGround * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
        to.damageScaleGroundExplosive   * fromState.participationGround * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
        to.damageScaleGroundNormal      * fromState.participationGround * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
        to.damageScaleAirConcussive     * fromState.participationAir    * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
        to.damageScaleAirExplosive      * fromState.participationAir    * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
        to.damageScaleAirNormal         * fromState.participationAir    * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    output.damage += Math.min(
      to.totalHealth - toState.damage,
      damagePerFrameTimesUnits * frameStep * ratioAlive / to.totalUnits)
    
    output
  }
  
  private def livingUnitsRatio(avatar:BattleEstimationUnit, damage:Double):Double = {
    Math.max(0.0, Math.ceil((avatar.totalHealth - damage) / avatar.totalHealth))
  }
  
  private def totalCost(frames: Int, state:BattleEstimationState) = {
    state.avatar.subjectiveValueCostPerFrame * frames +
      state.avatar.subjectiveValue * state.damage / state.avatar.totalHealth
  }
}
