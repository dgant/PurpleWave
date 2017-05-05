package Information.Battles.Estimation

import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.PurpleMath

abstract class BattleEstimationCalculator {
  
  val avatarUs      : BattleEstimationUnit
  val avatarEnemy   : BattleEstimationUnit
  val tacticsUs     : TacticsOptions
  val tacticsEnemy  : TacticsOptions
  
  var result = new BattleEstimationResult
  
  def recalculate() {
    result = new BattleEstimationResult
    if (avatarUs.totalUnits == 0 || avatarEnemy.totalUnits == 0) return
    
    val frameStep   = 24
    val frames      = frameStep * 12
    var stateUs     = BattleEstimationCalculationState(avatarUs,    tacticsUs,    -avatarUs.pixelsFromFocus,    0.0)
    var stateEnemy  = BattleEstimationCalculationState(avatarEnemy, tacticsEnemy, avatarEnemy.pixelsFromFocus,  0.0)
    
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
    stateThis: BattleEstimationCalculationState,
    stateThat: BattleEstimationCalculationState)
      : BattleEstimationCalculationState = {
    
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
    stateThis: BattleEstimationCalculationState,
    stateThat: BattleEstimationCalculationState) {
    
    val distance                  = Math.abs(stateThis.x - stateThat.x)
    val expectedSpread            = Math.sqrt(stateThis.avatar.totalUnits * 32.0)
    val spreadFactor              = PurpleMath.clampToOne(expectedSpread / stateThis.spread)
    stateThis.participationGround = spreadFactor * PurpleMath.nanToOne(PurpleMath.clampToOne(stateThis.avatar.rangePixelsGround / (distance - stateThis.spread)))
    stateThis.participationAir    = spreadFactor * PurpleMath.nanToOne(PurpleMath.clampToOne(stateThis.avatar.rangePixelsAir    / (distance - stateThis.spread)))
  }
  
  private def clampToOne(value:Double):Double = Math.max(0.0, Math.min(1.0, value))
  
  private def dealDamage(
    frameStep : Int,
    fromState : BattleEstimationCalculationState,
    toState   : BattleEstimationCalculationState)
      : BattleEstimationCalculationState = {
    
    val output = toState.copy()
    
    val from        = fromState.avatar
    val to          = toState.avatar
    val fromTactics = fromState.tactics
    val ratioAlive  = livingUnitsRatio(fromState.avatar, fromState.damage)
    val airFocus    = if (fromTactics.has(Tactics.Focus.Ground)) 0.0 else if (fromTactics.has(Tactics.Focus.Air)) 1.0 else to.totalFlyers / to.totalUnits
    val groundFocus = 1.0 - airFocus
    
    val damagePerFrame =
      to.damageScaleGroundConcussive  * from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * fromState.participationGround * groundFocus +
      to.damageScaleGroundExplosive   * from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * fromState.participationGround * groundFocus +
      to.damageScaleGroundNormal      * from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * fromState.participationGround * groundFocus +
      to.damageScaleAirConcussive     * from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * fromState.participationAir    * airFocus +
      to.damageScaleAirExplosive      * from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * fromState.participationAir    * airFocus +
      to.damageScaleAirNormal         * from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * fromState.participationAir    * airFocus
    
    output.damage += Math.min(
      to.totalHealth - toState.damage,
      damagePerFrame * frameStep * ratioAlive)
    
    output
  }
  
  private def livingUnitsRatio(avatar:BattleEstimationUnit, damage:Double):Double = {
    Math.max(0.0, Math.ceil((avatar.totalHealth - damage) / avatar.totalHealth))
  }
  
  private def totalCost(frames: Int, state:BattleEstimationCalculationState) = {
    state.avatar.subjectiveValueCostPerFrame * frames +
      state.avatar.subjectiveValue * state.damage / state.avatar.totalHealth
  }
}
