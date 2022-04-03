package Information.Battles.Prediction.Skimulation

import Information.Battles.Types.{Battle, Team}
import Lifecycle.With
import Mathematics.Maff
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.LightYear

object Skimulator {

  def durabilityFrames(team: Team): Double = team.meanTotalHealth / team.opponent.meanDpf

  def predict(battle: Battle): Unit = {

    // Calculate unit distance
    battle.teams.foreach(team => team.units.foreach(unit => {
      unit.skimDistanceToEngage = Math.max(0d,
        Maff.min(
          team.opponent.units.map(e =>
            (if (unit.isEnemy || unit.inRangeToAttack(e)) unit.pixelDistanceEdge(e) else Math.max(0, unit.pixelDistanceTravelling(e.pixel) - unit.pixelRangeAgainst(e)))
              - Math.max(
              if (e.canAttack(unit)) e.pixelRangeAgainst(unit) else 0,
              if (unit.canAttack(e)) unit.pixelRangeAgainst(e) else 0))).getOrElse(0d))}))

    // Estimate distance of hidden enemy units, conservatively expecting them to travel with the rest of the army
    // Note that this can significantly swing our perception of hidden army strength
    if (battle.enemy.units.exists(_.visible)) {
      val enemyMeanVisibleDistance = Maff.mean(battle.enemy.units.view.filter(_.visible).map(_.skimDistanceToEngage))
      battle.enemy.units.view.filterNot(_.visible).foreach(u => u.skimDistanceToEngage = Maff.clamp(u.skimDistanceToEngage - u.topSpeed * With.framesSince(u.lastSeen), u.skimDistanceToEngage, enemyMeanVisibleDistance))
    }

    battle.teams.foreach(team => team.skimMeanWarriorDistanceToEngage = Maff.meanOpt(team.units.view.filter(MatchWarriors).map(_.skimDistanceToEngage)).getOrElse(LightYear()))
    battle.teams.foreach(team => team.units.foreach(unit => {
      // Calculate speed, accounting reasonably for immobile units
      val speed                 = Math.max(unit.topSpeed, if (unit.isFriendly) 0 else if (MatchTank(unit) && ! unit.visible) Terran.SiegeTankUnsieged.topSpeed else Protoss.Reaver.topSpeed / 4)
      val delayFrames           = Math.max(unit.cooldownLeft, Maff.nanToOne((unit.skimDistanceToEngage - team.skimMeanWarriorDistanceToEngage) / speed / (if (unit.isFriendly) battle.speedMultiplier else 1.0)))
      val extensionFrames       = Maff.nanToOne((team.skimMeanWarriorDistanceToEngage - unit.skimDistanceToEngage) / team.meanAttackerSpeed)
      val teamDurabilityFrames  = Maff.clamp(Maff.nanToOne(team.meanTotalHealth / team.opponent.meanDpf), 12, 120)
      unit.skimDelay            = Maff.clamp(Maff.nanToOne(delayFrames / teamDurabilityFrames), 0.0, 1.0)
      unit.skimExtension        = Maff.clamp(Maff.nanToZero(extensionFrames / teamDurabilityFrames), 0.0, 0.75)
      unit.skimPresence         = 1.0 - Math.max(unit.skimDelay, unit.skimExtension)
    }))

    // Calculate unit strength
    battle.teams.foreach(team => team.units.foreach(unit => {
      val energy = if (unit.isFriendly) unit.energy else 100
      val player = unit.player
      val casts75 = Math.floor(unit.energy / 75)
      val casts100 = Math.floor(unit.energy / 100)
      val casts125 = Math.floor(unit.energy / 125)
      val casts150 = Math.floor(unit.energy / 150)
      unit.skimStrength = Maff.nanToZero(unit.unitClass.skimulationValue * unit.totalHealth / unit.unitClass.maxTotalHealth)

      // Count basic upgrades
      unit.skimStrength *= 1.0 + 0.15 * (unit.armorHealth - unit.unitClass.armor)
      if (unit.unitClass.effectiveAirDamage > 0)
        unit.skimStrength *= unit.damageOnHitAir / unit.unitClass.effectiveAirDamage
      else if (unit.unitClass.effectiveGroundDamage> 0)
        unit.skimStrength *= unit.damageOnHitGround / unit.unitClass.effectiveGroundDamage

      // Consider high ground advantage
      if (unit.isFriendly && unit.canAttack && unit.effectiveRangePixels > 64 && ! unit.flying) {
        if (unit.presumptiveTarget.exists(_.altitude > unit.altitude)) {
          unit.skimStrength *= 0.5
        } else if (unit.presumptiveTarget.exists(t => t.altitude < unit.pixelToFireAt(t).altitude)) {
          unit.skimStrength *= 1.5
        }
      }

      // Consider detection
      if ( ! team.opponent.detectors.exists(d => d.canMove || d.isEnemy)) {
        if (Terran.Wraith(unit) && Terran.WraithCloak(player)) unit.skimStrength *= 5
        if (Protoss.DarkTemplar(unit)) unit.skimStrength *= 15
        if (Zerg.Lurker(unit)) unit.skimStrength *= 5
      }

      lazy val enemySize = team.opponent.attackersCastersCount

      // Count other unit properties
      if (unit.canStim)                                                   unit.skimStrength *= 1.2
      if (unit.ensnared)                                                  unit.skimStrength *= 0.5
      if (Terran.Marine(unit)   && Terran.MarineRange(player))            unit.skimStrength *= 1.3
      if (Terran.Marine(unit)   && Terran.Stim(player))                   unit.skimStrength *= 1.3
      if (Terran.Firebat(unit)  && Terran.Stim(player))                   unit.skimStrength *= 1.3
      if (Terran.Medic(unit))                                             unit.skimStrength *= Maff.clamp(team.attackersBioCount / 3.0 - (team.count(Terran.Medic) - 1), 0.0, 2.0)
      if (Terran.Vulture(unit)  && Terran.VultureSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Archon(unit)  && unit.matchups.targetsInRange.nonEmpty) unit.skimStrength *= 1.5
      if (Protoss.Carrier(unit) && unit.isFriendly)                       unit.skimStrength *= unit.interceptors.size / 8.0
      if (Protoss.Reaver(unit))                                           unit.skimStrength *= Maff.clamp(team.opponent.count(MatchGroundWarriors) / 12, 1.0, 2.5)
      if (Protoss.Reaver(unit))                                           unit.skimStrength *= Maff.clamp(team.count(Protoss.Shuttle) / team.count(Protoss.Reaver), 1.0, if (Protoss.ShuttleSpeed(unit.player)) 2.0 else 1.5)
      if (Protoss.Zealot(unit)  && Protoss.ZealotSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Dragoon(unit) && Protoss.DragoonRange(player))          unit.skimStrength *= 1.5
      if (Zerg.Lurker(unit)     && ! unit.burrowed)                       unit.skimStrength *= 0.8
      if (Zerg.Zergling(unit)   && Zerg.ZerglingSpeed(player))            unit.skimStrength *= 1.2
      if (Zerg.Zergling(unit)   && Zerg.ZerglingAttackSpeed(player))      unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskSpeed(player))           unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskRange(player))           unit.skimStrength *= 1.2

      // Major upgrade thresholds
      if (Protoss.Zealot(unit) && unit.presumptiveTarget.exists(t => Zerg.Zergling(t) && unit.damageUpgradeLevel > t.armorHealth)) unit.skimStrength *= 1.3

      // Count spells
      unit.skimMagic = 0
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.ScienceVessel(unit))                                  * casts100  * 1.5)
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Battlecruiser(unit))                                  * casts150  * 2.5)
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Ghost(unit)         && Terran.Lockdown(player))       * casts100  * Maff.clamp(team.opponent.count(MatchMechWarriors)   / 1,  1.0, 1.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.HighTemplar(unit)  && Protoss.PsionicStorm(player))  * casts75   * Maff.clamp(team.opponent.count(MatchWarriors)       / 8,  1.0, 2.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.DarkArchon(unit)   && Protoss.Maelstrom(player))     * casts100  * Maff.clamp(team.opponent.count(MatchBioWarriors)    / 4,  1.0, 4.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Arbiter(unit)      && Protoss.Stasis(player))        * casts100  * Maff.clamp(team.opponent.count(MatchWarriors)       / 4,  1.0, 4.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Corsair(unit)      && Protoss.DisruptionWeb(player)) * casts125  * Maff.clamp(team.opponent.count(MatchGroundWarriors) / 4,  1.0, 3.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Queen(unit)           && Zerg.Ensnare(player))          * casts100  * Maff.clamp(team.opponent.count(MatchWarriors)       / 12, 1.0, 2.0))
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Defiler(unit))                                          * casts100  * Maff.clamp(team.opponent.count(MatchWarriors)       / 4,  1.0, 4.0))
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
      incapable ||= unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm
      if (incapable) { unit.skimStrength *= -0.25 } // Encourage fighting when units are disabled!
    }))

    // Calculate team properties
    // TODO: Terrain influence: Choke
    battle.teams.foreach(team => team.units.foreach(unit => {
      team.skimStrengthTotal    += unit.skimStrength
      team.skimStrengthAir      += unit.skimStrength * Maff.fromBoolean(unit.flying)
      team.skimStrengthGround   += unit.skimStrength * Maff.fromBoolean(! unit.flying)
      team.skimStrengthVsAir    += unit.skimStrength * Maff.fromBoolean(unit.canAttackAir || ! unit.canAttack)
      team.skimStrengthVsGround += unit.skimStrength * Maff.fromBoolean(unit.canAttackGround || ! unit.canAttack || (Protoss.Corsair(unit) && unit.energy >= 125 && Protoss.DisruptionWeb(unit.player)))
    }))
  }
}
