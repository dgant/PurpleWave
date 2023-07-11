package Information.Battles.Prediction.Skimulation

import Information.Battles.Types.{Battle, Team}
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Seconds
import Utilities.UnitFilters._
import Utilities.{?, LightYear}

object Skimulator {

  @inline private def distanceToEngage(unit: UnitInfo, team: Team): Double = {
    val targets = ?(Terran.Medic(unit),
      team.units.view.filter(_.unitClass.isOrganic),
      Maff.orElse(unit.presumptiveTarget, team.opponent.units))
    Maff.min(targets.view.map(unit.pixelDistanceEdge)).getOrElse(LightYear())
  }

  def predict(battle: Battle): Unit = {

    // Calculate unit distance
    if (battle.isGlobal) {
      // Assume all units are charging from a line at max distance like a dumb cinematic battle
      val maxEffectiveRange = Maff.max(battle.units.map(_.effectiveRangePixels)).getOrElse(0.0)
      battle.units.foreach(u => u.skimDistanceToEngage = maxEffectiveRange - u.effectiveRangePixels)
    } else {
      // Since we're deciding whether to attack into the enemy, assume the enemy can regroup in the time it takes for us to engage.
      // Manifest this as giving enemies bonus range proportional to the time before the fight gets underway
      //
      // Limit the scale of this effect, since optimistic results don't hurt us until we actually get into combat
      var enemyBonusTravelFrames = Seconds(3)()

      battle.us.units.foreach(unit => {
        unit.skimDistanceToEngage = Math.max(0.0, distanceToEngage(unit, battle.us) - unit.effectiveRangePixels)
        enemyBonusTravelFrames    = Math.min(enemyBonusTravelFrames, unit.framesToTravelPixels(unit.skimDistanceToEngage))
      })
      // Do a pass of visible units first, in order to estimate reasonable values for hidden units
      var visibleDistanceToTargetNumerator = 0d
      var visibleDistanceToTargetDenominator = 0d
      battle.enemy.units.foreach(unit => {
        if (unit.visible) {
          val distanceToTarget = distanceToEngage(unit, battle.enemy)
          if (distanceToTarget < LightYear()) {
            visibleDistanceToTargetNumerator    += distanceToTarget
            visibleDistanceToTargetDenominator  += 1
          }
          unit.skimDistanceToEngage = Math.max(0.0, visibleDistanceToTargetNumerator - unit.effectiveRangePixels - unit.topSpeed * enemyBonusTravelFrames)
        }
      })
      val visibleDistanceToTarget = visibleDistanceToTargetNumerator / Math.max(1d, visibleDistanceToTargetDenominator)
      // Estimate distance of hidden enemy units, conservatively expecting them to travel with the rest of the army
      battle.enemy.units.foreach(unit => {
        if ( ! unit.visible) {
          val distanceToTarget      = distanceToEngage(unit, battle.enemy)
          val distanceFloor         = distanceToTarget - unit.topSpeed * With.framesSince(unit.lastSeen)
          unit.skimDistanceToEngage = Maff.vmax(
            0.0,
            distanceFloor,
            Math.min(distanceToTarget, visibleDistanceToTarget + 32 * 8) - unit.effectiveRangePixels - unit.topSpeed * enemyBonusTravelFrames)
        }
      })
    }

    // Estimate distance of hidden enemy units, conservatively expecting them to travel with the rest of the army
    // Note that this can significantly swing our perception of hidden army strength
    if (battle.enemy.units.exists(_.visible)) {
      val enemyMeanVisibleDistance = Maff.mean(
        Maff.orElse(
          battle.enemy.attackersNonWorker,
          battle.enemy.attackers,
          battle.enemy.units).view.filter(_.visible).map(_.skimDistanceToEngage))
      battle.enemy.units.view.filterNot(_.visible).foreach(u => u.skimDistanceToEngage = Maff.clamp(u.skimDistanceToEngage - u.topSpeed * With.framesSince(u.lastSeen), u.skimDistanceToEngage, enemyMeanVisibleDistance))
    }

    battle.teams.foreach(team => team.skimMeanWarriorDistanceToEngage = Maff.meanOpt(team.units.view.filter(IsWarrior).map(_.skimDistanceToEngage)).getOrElse(LightYear()))
    battle.teams.foreach(team => team.units.foreach(unit => {
      // Calculate speed, accounting reasonably for immobile units
      val speed                 = Math.max(unit.topSpeed, if (unit.isFriendly) 0 else if (IsTank(unit) && ! unit.visible) Terran.SiegeTankUnsieged.topSpeed else Protoss.Reaver.topSpeed * 0.25)
      val delayFrames           = Math.max(unit.cooldownLeft, Maff.nanToOne((unit.skimDistanceToEngage - team.skimMeanWarriorDistanceToEngage) / speed / ?(unit.isFriendly, battle.speedMultiplier, 1.0)))
      val extensionFrames       = Maff.nanToOne((team.skimMeanWarriorDistanceToEngage - unit.skimDistanceToEngage) / team.meanAttackerSpeed)
      val teamDurabilityFrames  = Maff.clamp(Maff.nanToOne(team.meanAttackerHealth  / team.opponent.meanDpf), 12,   240)
      unit.skimDelay            = Maff.clamp(Maff.nanToOne(delayFrames              / teamDurabilityFrames),  0.0,  1.0)
      unit.skimExtension        = Maff.clamp(Maff.nanToZero(extensionFrames         / teamDurabilityFrames),  0.0,  0.75)
      unit.skimPresence         = 1.0 - Math.max(unit.skimDelay, unit.skimExtension)
    }))

    // Calculate unit strength
    battle.teams.foreach(team => team.units.foreach(unit => {
      val opponent      = team.opponent
      val energy        = ?(unit.isFriendly, unit.energy, 100)
      val player        = unit.player
      val casts75       = Math.floor(unit.energy / 75)
      val casts100      = Math.floor(unit.energy / 100)
      val casts125      = Math.floor(unit.energy / 125)
      val casts150      = Math.floor(unit.energy / 150)
      unit.skimStrength = Maff.nanToZero(unit.unitClass.skimulationValue * unit.totalHealth / unit.unitClass.maxTotalHealth)

      // Count basic upgrades
      // We do mean(1, multiplier) to acknowledge that half of a unit's contribution to the fight is just tanking.
      // With better modeling of damage dropoff that might not be necessary
      unit.skimStrength *= Maff.vmean(1.0, Maff.nanToOne((team.meanDamageOnHit - unit.unitClass.armor) / (opponent.meanDamageOnHit - (unit.armorHealth - unit.unitClass.armor))))
      if      (unit.unitClass.effectiveAirDamage    > 0)  unit.skimStrength *= Maff.vmean(1.0,  Maff.nanToOne((unit.damageOnHitAir.toDouble    - opponent.meanArmorAir)    / (unit.unitClass.effectiveAirDamage    - opponent.meanArmorAir)))
      else if (unit.unitClass.effectiveGroundDamage > 0)  unit.skimStrength *= Maff.vmean(1.0,  Maff.nanToOne((unit.damageOnHitGround.toDouble - opponent.meanArmorGround) / (unit.unitClass.effectiveGroundDamage - opponent.meanArmorGround)))

      // Consider high ground advantage
      if ( ! battle.isGlobal && unit.isFriendly && unit.canAttack && unit.effectiveRangePixels > 64 && ! unit.flying) {
        if (unit.presumptiveTarget.exists(_.altitude > unit.altitude)) {
          unit.skimStrength *= 0.75 // 0.5 in terms of DPS, but see above remarks about treating units partly as sponges
        } else if (unit.presumptiveTarget.exists(t => t.altitude < unit.pixelToFireAtSimple(t).altitude)) {
          unit.skimStrength *= 1.25 // 1.5 in terms of DPS, but see above remarks about treating units partly as sponges
        }
      }

      // Consider detection
      if (unit.isEnemy && ! team.opponent.hasDetection) {
        if (Terran.Wraith(unit) && Terran.WraithCloak(player))  unit.skimStrength *= 5
        if (Protoss.DarkTemplar(unit))                          unit.skimStrength *= 15
        if (Zerg.Lurker(unit))                                  unit.skimStrength *= 5
      }

      lazy val maxEffectiveMedics = team.attackersBioCount / 3.0

      // Count other unit properties
      if (unit.canStim)                                                   unit.skimStrength *= ?(unit.stimmed, 1.2, 1.1) // Consider whether they already have taken the damage hit from stim
      if (unit.ensnared)                                                  unit.skimStrength *= 0.5
      if (Terran.Marine(unit)   && Terran.MarineRange(player))            unit.skimStrength *= 1.3
      if (Terran.Marine(unit)   && Terran.Stim(player))                   unit.skimStrength *= 1.3
      if (Terran.Firebat(unit)  && Terran.Stim(player))                   unit.skimStrength *= 1.3
      if (Terran.Medic(unit))                                             unit.skimStrength *= Maff.clamp(maxEffectiveMedics / Math.max(1, team.count(Terran.Medic)), 0.0, 1.0)
      if (Terran.Vulture(unit)  && Terran.VultureSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Archon(unit)  && unit.matchups.targetsInRange.nonEmpty) unit.skimStrength *= 1.5
      if (Protoss.Carrier(unit) && unit.isFriendly)                       unit.skimStrength *= unit.interceptors.size * Maff.inv8
      if (Protoss.Reaver(unit))                                           unit.skimStrength *= Maff.clamp(team.opponent.count(IsGroundWarrior) / 12, 1.0, 2.5)
      if (Protoss.Reaver(unit))                                           unit.skimStrength *= Maff.clamp(team.count(Protoss.Shuttle) / team.count(Protoss.Reaver), 1.0, if (Protoss.ShuttleSpeed(unit.player)) 2.0 else 1.5)
      if (Protoss.Zealot(unit)  && Protoss.ZealotSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Dragoon(unit) && Protoss.DragoonRange(player))          unit.skimStrength *= 1.5
      if (Zerg.Zergling(unit)   && Zerg.ZerglingSpeed(player))            unit.skimStrength *= 1.2
      if (Zerg.Zergling(unit)   && Zerg.ZerglingAttackSpeed(player))      unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskSpeed(player))           unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskRange(player))           unit.skimStrength *= 1.2
      if (Zerg.Lurker(unit)     && ! unit.burrowed)                       unit.skimStrength *= 0.8

      // Major upgrade thresholds
      if (Protoss.Zealot(unit) && unit.presumptiveTarget.exists(t => Zerg.Zergling(t) && unit.damageUpgradeLevel > t.armorHealth)) unit.skimStrength *= 1.3

      // Count spells
      unit.skimMagic = 0
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.ScienceVessel(unit))                                  * casts100  * 1.5)
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Battlecruiser(unit))                                  * casts150  * 2.5)
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Ghost(unit)         && Terran.Lockdown(player))       * casts100  * Maff.clamp(team.opponent.count(IsMechWarrior)   / 1,  1.0, 1.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.HighTemplar(unit)  && Protoss.PsionicStorm(player))  * casts75   * Maff.clamp(team.opponent.count(IsWarrior)       / 8,  1.0, 2.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.DarkArchon(unit)   && Protoss.Maelstrom(player))     * casts100  * Maff.clamp(team.opponent.count(IsBioWarrior)    / 4,  1.0, 4.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Arbiter(unit)      && Protoss.Stasis(player))        * casts100  * Maff.clamp(team.opponent.count(IsWarrior)       / 4,  1.0, 4.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Corsair(unit)      && Protoss.DisruptionWeb(player)) * casts125  * Maff.clamp(team.opponent.count(IsGroundWarrior) / 4,  1.0, 3.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Queen(unit)           && Zerg.Ensnare(player))          * casts100  * Maff.clamp(team.opponent.count(IsWarrior)       / 12, 1.0, 2.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Defiler(unit))                                          * casts100  * Maff.clamp(team.opponent.count(IsWarrior)       / 4,  1.0, 4.0))
      unit.skimStrength += unit.skimMagic

      // Count presence
      unit.skimStrength *= unit.skimPresence

      // Count incapability
      var incapable = ! unit.complete
      incapable ||= unit.stasised
      incapable ||= unit.maelstrommed
      incapable ||= unit.unitClass.canAttack && unit.underDisruptionWeb
      incapable ||= unit.unitClass.canBeStormed && unit.underStorm // Units under storm are presumably preoccupied with dodging
      incapable ||= unit.lockedDown
      incapable ||= unit.underDarkSwarm && unit.unitClass.affectedByDarkSwarm
      if (incapable) { unit.skimStrength *= -0.25 } // Encourage fighting when units are disabled!
    }))

    // Calculate team properties
    // TODO: Terrain influence: Choke
    battle.teams.foreach(team => team.units.foreach(unit => {
      team.skimStrengthTotal    += unit.skimStrength
      team.skimStrengthAir      += unit.skimStrength * Maff.fromBoolean(unit.flying)
      team.skimStrengthGround   += unit.skimStrength * Maff.fromBoolean( ! unit.flying)
      team.skimStrengthVsAir    += unit.skimStrength * Maff.fromBoolean(unit.canAttackAir     || ! unit.canAttack)
      team.skimStrengthVsGround += unit.skimStrength * Maff.fromBoolean(unit.canAttackGround  || ! unit.canAttack || (Protoss.Corsair(unit) && unit.skimMagic > 0))
    }))
  }
}
