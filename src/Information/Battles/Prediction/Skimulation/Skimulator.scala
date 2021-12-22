package Information.Battles.Prediction.Skimulation

import Information.Battles.Types.{BattleLocal, Team}
import Mathematics.Maff
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.Forever

object Skimulator {

  def durabilityFrames(team: Team): Double = team.meanTotalHealth / team.opponent.meanDpf

  def predict(battle: BattleLocal): Unit = {

    // Calculate unit participation
    battle.teams.foreach(team => team.units.foreach(unit => {
      // TODO: Consider: Does this achieve everything the reference logic does?
      // TODO: Definitely doesn't include ground distance considerations, which for some reason are enemy->friendly and not the reverse
      unit.skimDistanceToEngage = Math.max(0d,
        Maff.min(
          team.opponent.units.map(e =>
            unit.pixelDistanceEdge(e)
              - Math.max(
              if (e.canAttack(unit)) e.pixelRangeAgainst(unit) else 0,
              if (unit.canAttack(e)) unit.pixelRangeAgainst(e) else 0))).getOrElse(0d))}))
    battle.teams.foreach(team => team.skimMeanWarriorDistanceToEngage = Maff.meanOpt(team.units.view.filter(MatchWarriors).map(_.skimDistanceToEngage)).getOrElse(Forever()))
    battle.teams.foreach(team => team.units.foreach(unit => {
      val delayFrames           = Maff.nanToOne((unit.skimDistanceToEngage - team.skimMeanWarriorDistanceToEngage) / unit.topSpeed / (if (unit.isFriendly) battle.speedMultiplier else 1.0))
      val extensionFrames       = Maff.nanToOne((team.skimMeanWarriorDistanceToEngage - unit.skimDistanceToEngage) / team.meanAttackerSpeed)
      val teamDurabilityFrames  = Maff.clamp(Maff.nanToOne(team.meanTotalHealth / team.opponent.meanDpf), 12, 120)
      unit.skimDelay            = Maff.clamp(Maff.nanToOne(delayFrames / teamDurabilityFrames), 0.0, 0.9)
      unit.skimExtension        = Maff.clamp(Maff.nanToZero(extensionFrames / teamDurabilityFrames), 0.0, 0.75)
      unit.skimPresence         = 1.0 - Math.max(unit.skimDelay, unit.skimExtension)
    }))
    // Boost presence of hidden enemy units, assuming they've come along.
    // Note that this can significantly swing our perception of hidden army strength
    val enemyMinVisiblePresence = Maff.min(battle.enemy.units.view.filter(_.visible).map(_.skimPresence)).getOrElse(1.0)
    battle.enemy.units.view.filterNot(_.visible).foreach(u => u.skimPresence = Math.max(u.skimPresence, enemyMinVisiblePresence))

    // Calculate unit strength
    battle.teams.foreach(team => team.units.foreach(unit => {
      val energy = if (unit.isFriendly) unit.energy else 100
      val player = unit.player
      val casts75 = Math.floor(unit.energy / 75)
      val casts100 = Math.floor(unit.energy / 100)
      val casts125 = Math.floor(unit.energy / 125)
      val casts150 = Math.floor(unit.energy / 150)
      //TODO: Splash value
      //TODO: Medic value
      //TODO: Integrate judgments
      //TODO: Consider weighing HP over shields
      //TODO: Enshuttled Reavers
      unit.skimStrength = Maff.nanToZero(unit.unitClass.skimulationValue * unit.totalHealth / unit.unitClass.maxTotalHealth)
      unit.skimStrength *= (if (Protoss.Reaver(unit) && unit.isFriendly) unit.interceptors.size / 8.0 else 1.0)

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

      // Count other unit properties
      if (unit.canStim)                                                   unit.skimStrength *= 1.2
      if (unit.ensnared)                                                  unit.skimStrength *= 0.5
      if (Terran.Marine(unit)   && Terran.MarineRange(player))            unit.skimStrength *= 1.2
      if (Terran.Medic(unit))                                             unit.skimStrength *= 1.0 // TODO: Cap on bio allies
      if (Terran.Vulture(unit)  && Terran.VultureSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Archon(unit)  && unit.matchups.targetsInRange.nonEmpty) unit.skimStrength *= 1.5
      if (Protoss.Carrier(unit) && unit.isFriendly)                       unit.skimStrength *= unit.interceptors.size / 8.0
      if (Protoss.Reaver(unit)  && unit.isFriendly && unit.scarabs == 0)  unit.skimStrength *= 0
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
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Battlecruiser(unit))                                  * casts150  * 2.5) // TODO: Max value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Ghost(unit)         && Terran.Lockdown(player))       * casts100  * 2.5) // TODO: Max mechanical value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.HighTemplar(unit)  && Protoss.PsionicStorm(player))  * casts75   * 2.5) // TODO: Capped by fraction of total value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.DarkArchon(unit)   && Protoss.Maelstrom(player))     * casts100  * 4)   // TODO: Capped by max bio value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Arbiter(unit)      && Protoss.Stasis(player))        * casts100  * 5)   // TODO: Capped by half max value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Queen(unit)           && Zerg.Ensnare(player))          * casts100  * 6)   // TODO: Capped by team melee valud
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Defiler(unit))                                          * casts100  * 6)   // TODO: Capped by team melee valud
      unit.skimStrength += unit.skimMagic

      // Count presence
      unit.skimStrength *= unit.skimPresence

      // Count incapability
      var incapable = ! unit.complete
      incapable ||= unit.stasised
      incapable ||= unit.maelstrommed
      incapable ||= unit.underDisruptionWeb
      incapable ||= unit.lockedDown
      incapable ||= unit.underDarkSwarm && ! unit.unitClass.unaffectedByDarkSwarm
      if (incapable) { unit.skimStrength = 0 }
    }))

    // Calculate team properties
    // TODO: Terrain influence: Choke
    battle.teams.foreach(team => team.units.foreach(unit => {
      team.skimStrengthTotal    += unit.skimStrength
      team.skimStrengthAir      += unit.skimStrength * Maff.fromBoolean(unit.flying)
      team.skimStrengthGround   += unit.skimStrength * Maff.fromBoolean(! unit.flying)
      team.skimStrengthVsAir    += unit.skimStrength * Maff.fromBoolean(unit.canAttackAir || ! unit.canAttack)
      team.skimStrengthVsGround += unit.skimStrength * Maff.fromBoolean(unit.canAttackGround || ! unit.canAttack)
    }))

    battle.predictionComplete = true
  }
}
